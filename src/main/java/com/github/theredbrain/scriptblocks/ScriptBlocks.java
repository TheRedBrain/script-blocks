package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.compatibility.LootableCompat;
import com.github.theredbrain.scriptblocks.compatibility.RPGInventoryCompat;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.EventsRegistry;
import com.github.theredbrain.scriptblocks.registry.GameRulesRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemComponentRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemGroupRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemRegistry;
import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import com.github.theredbrain.scriptblocks.registry.ServerPacketRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.registry.StructurePlacementTypesRegistry;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScriptBlocks implements ModInitializer {
	public static final String MOD_ID = "scriptblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ServerConfig SERVER_CONFIG;

	public static RegistryEntry<StatusEffect> HOUSING_OWNER_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_CO_OWNER_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_TRUSTED_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_GUEST_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_STRANGER_EFFECT;
	public static RegistryEntry<StatusEffect> BUILDING_MODE;
	public static RegistryEntry<StatusEffect> PORTAL_RESISTANCE_EFFECT;
	public static RegistryEntry<StatusEffect> ADVENTURE_EFFECT;

	public static final boolean isLootablesLoaded = FabricLoader.getInstance().isModLoaded("lootables");
	public static final boolean isRPGInventoryLoaded = FabricLoader.getInstance().isModLoaded("rpginventory");

	public static void supplyLootableLoot(Identifier identifier, ServerWorld world, ServerPlayerEntity serverPlayerEntity, Vec3d pos, int rolls, int choices, boolean withChoice, @Nullable ItemStack itemStack) {
		if (isLootablesLoaded) {
			LootableCompat.supplyLootableLoot(identifier, world, serverPlayerEntity, pos, rolls, choices, withChoice, itemStack);
		} else {
			info("Tried to supply loot via Lootables, but the mod is not installed!");
		}
	}

	public static void setCurrentPVPControllerBlockPosition(PlayerEntity playerEntity, Optional<BlockPos> currentPVPControllerBlockPosition) {
		((DuckPlayerEntityMixin) playerEntity).scriptblocks$setCurrentPVPControllerBlockPosition(currentPVPControllerBlockPosition);
	}

	public static void addPlayerAndTeamToPVPControllerBlock(Team team, PlayerEntity playerEntity, ServerWorld serverWorld, Optional<BlockPos> currentPVPControllerBlockPosition) {
		if (currentPVPControllerBlockPosition.isPresent()) {
			if (serverWorld.getBlockEntity(currentPVPControllerBlockPosition.get()) instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				pvpControllerBlockEntity.addPlayerAndTeam(team, playerEntity);
			}
		}
	}

	public static void removePlayerFromPVPControllerBlock(PlayerEntity playerEntity, ServerWorld serverWorld, Optional<BlockPos> currentPVPControllerBlockPosition) {
		if (currentPVPControllerBlockPosition.isPresent()) {
			if (serverWorld.getBlockEntity(currentPVPControllerBlockPosition.get()) instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				pvpControllerBlockEntity.removePlayer(playerEntity);
			}
		}
	}

	public static ItemStack getRPGEquipmentStack(PlayerEntity playerEntity, int index) {
		if (isRPGInventoryLoaded) {
			return RPGInventoryCompat.getRPGEquipmentStack(playerEntity, index);
		} else {
			return ItemStack.EMPTY;
		}
	}

	public static void setRPGEquipmentStack(PlayerEntity playerEntity, int index, ItemStack stack) {
		if (isRPGInventoryLoaded) {
			RPGInventoryCompat.setRPGEquipmentStack(playerEntity, index, stack);
		}
	}

	@Nullable
	public static MutablePair<RegistryKey<World>, MutablePair<BlockPos, MutablePair<Double, Double>>> getPVPRespawnPosition(Team team, ServerPlayerEntity serverPlayerEntity, boolean endOfBattle) {
		Optional<BlockPos> optionalBlockPos = ((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$getCurrentPVPControllerBlockPosition();
		World world = serverPlayerEntity.getWorld();
		if (optionalBlockPos.isPresent() && team != null && world instanceof ServerWorld serverWorld) {
			String teamId = team.getName();
			BlockEntity blockEntity = serverWorld.getBlockEntity(optionalBlockPos.get());
			if (blockEntity instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				MutablePair<BlockPos, MutablePair<Double, Double>> teamRespawnPos = pvpControllerBlockEntity.getTeamRespawnPosition(teamId, endOfBattle);
				if (teamRespawnPos != null) {
					if (endOfBattle) {
						pvpControllerBlockEntity.removePlayer(serverPlayerEntity);
					}
					return new MutablePair<>(
							serverWorld.getRegistryKey(),
							new MutablePair<>(
									teamRespawnPos.getLeft(),
									new MutablePair<>(
											teamRespawnPos.getRight().getLeft(),
											teamRespawnPos.getRight().getRight()
									)
							)
					);
				}
			}
		}
		return null;
	}

	@Override
	public void onInitialize() {
		LOGGER.info("This was scripted!");

		// Config
		SERVER_CONFIG = ConfigApiJava.registerAndLoadConfig(ServerConfig::new, RegisterType.BOTH);

		// Packets
		ServerPacketRegistry.init();

		// Registry
		ItemComponentRegistry.init();
		BlockRegistry.init();
		EntityRegistry.init();
		DimensionsManager.init();
		EventsRegistry.initializeEvents();
		CustomDynamicRegistries.init();
		ItemRegistry.init();
		ItemGroupRegistry.init();
		ScreenHandlerTypesRegistry.registerAll();
		StatusEffectsRegistry.registerEffects();
		GameRulesRegistry.init();
		StructurePlacementTypesRegistry.register();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static void info(String message) {
		LOGGER.info("[" + MOD_ID + "] [info]: " + message);
	}

	public static void warn(String message) {
		LOGGER.warn("[" + MOD_ID + "] [warn]: " + message);
	}

	public static void debug(String message) {
		LOGGER.debug("[" + MOD_ID + "] [debug]: " + message);
	}

	public static void error(String message) {
		LOGGER.error("[" + MOD_ID + "] [error]: " + message);
	}
}