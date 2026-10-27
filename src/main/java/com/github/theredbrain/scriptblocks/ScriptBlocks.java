package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.compatibility.LootableCompat;
import com.github.theredbrain.scriptblocks.compatibility.RPGInventoryCompat;
import com.github.theredbrain.scriptblocks.registry.EventsRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemGroupRegistry;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataAttachments;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataComponents;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksItems;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksNetworkPackets;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStructurePlacementTypes;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ScriptBlocks implements ModInitializer {
	public static final String MOD_ID = "scriptblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final boolean isLootablesLoaded = FabricLoader.getInstance().isModLoaded("lootables");
	public static final boolean isRPGInventoryLoaded = FabricLoader.getInstance().isModLoaded("rpginventory");

	@Deprecated
	public static void supplyLootableLoot(Identifier identifier, ServerLevel world, ServerPlayer serverPlayerEntity, Vec3 pos, int rolls, int choices, boolean withChoice, @Nullable ItemStack itemStack) {
		if (isLootablesLoaded) {
			LootableCompat.supplyLootableLoot(identifier, world, serverPlayerEntity, pos, rolls, choices, withChoice, itemStack);
		} else {
			info("Tried to supply loot via Lootables, but the mod is not installed!");
		}
	}

	public static void setCurrentPVPControllerBlockPosition(Player playerEntity, Optional<BlockPos> currentPVPControllerBlockPosition) {
		DataAttachmentHelper.setCurrentPVPControllerBlockPosition(playerEntity, currentPVPControllerBlockPosition);
	}

	public static void addPlayerAndTeamToPVPControllerBlock(PlayerTeam team, Player playerEntity, ServerLevel serverWorld, Optional<BlockPos> currentPVPControllerBlockPosition) {
		if (currentPVPControllerBlockPosition.isPresent()) {
			if (serverWorld.getBlockEntity(currentPVPControllerBlockPosition.get()) instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				pvpControllerBlockEntity.addPlayerAndTeam(team, playerEntity);
			}
		}
	}

	public static void removePlayerFromPVPControllerBlock(Player playerEntity, ServerLevel serverWorld, Optional<BlockPos> currentPVPControllerBlockPosition) {
		if (currentPVPControllerBlockPosition.isPresent()) {
			if (serverWorld.getBlockEntity(currentPVPControllerBlockPosition.get()) instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				pvpControllerBlockEntity.removePlayer(playerEntity);
			}
		}
	}

	public static ItemStack getRPGEquipmentStack(Player playerEntity, int index) {
		if (isRPGInventoryLoaded) {
			return RPGInventoryCompat.getRPGEquipmentStack(playerEntity, index);
		} else {
			return ItemStack.EMPTY;
		}
	}

	public static void setRPGEquipmentStack(Player playerEntity, int index, ItemStack stack) {
		if (isRPGInventoryLoaded) {
			RPGInventoryCompat.setRPGEquipmentStack(playerEntity, index, stack);
		}
	}

	public static void trigger(ServerLevel serverWorld, BlockPos blockPos, boolean resets) {
		BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos);
		if (resets && blockEntity instanceof Resetable resetable) {
			resetable.reset();
		} else if (!resets && blockEntity instanceof Triggerable triggerable) {
			triggerable.trigger();
		}
	}

	@Nullable
	public static MutablePair<ResourceKey<Level>, MutablePair<BlockPos, MutablePair<Double, Double>>> getPVPRespawnPosition(PlayerTeam team, ServerPlayer serverPlayerEntity, boolean endOfBattle) {
		Optional<BlockPos> optionalBlockPos = DataAttachmentHelper.getCurrentPVPControllerBlockPosition(serverPlayerEntity);
		Level world = serverPlayerEntity.level();
		if (optionalBlockPos.isPresent() && team != null && world instanceof ServerLevel serverWorld) {
			String teamId = team.getName();
			BlockEntity blockEntity = serverWorld.getBlockEntity(optionalBlockPos.get());
			if (blockEntity instanceof PVPControllerBlockEntity pvpControllerBlockEntity) {
				MutablePair<BlockPos, MutablePair<Double, Double>> teamRespawnPos = pvpControllerBlockEntity.getTeamRespawnPosition(teamId, endOfBattle);
				if (teamRespawnPos != null) {
					if (endOfBattle) {
						pvpControllerBlockEntity.removePlayer(serverPlayerEntity);
					}
					return new MutablePair<>(
							serverWorld.dimension(),
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

		// Packets
		ScriptBlocksNetworkPackets.init();

		// Registry
		ScriptBlocksDataAttachments.bootstrap();
		ScriptBlocksDataComponents.bootstrap();
		ScriptBlocksBlocks.bootstrap();
		ScriptBlocksConfigs.bootstrap();
		ScriptBlocksEntities.init();
		DimensionsManager.bootstrap();
		EventsRegistry.initializeEvents();
		ScriptBlocksDynamicRegistries.init();
		ScriptBlocksItems.bootstrap();
		ItemGroupRegistry.init();
		ScriptBlocksScreenHandlerTypes.init();
		ScriptBlocksStatusEffects.bootstrap();
		ScriptBlocksStructurePlacementTypes.init();
	}

	public static void sendDeprecatedFeatureInfo(String message, @Nullable MinecraftServer minecraftServer) {
		if (minecraftServer != null) {
			if (ScriptBlocksConfigs.SERVER_CONFIG.enable_deprecated_feature_chat_message) {
				minecraftServer.getPlayerList().broadcastSystemMessage(Component.nullToEmpty(message), false);
			}
			info(message);
		}
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
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