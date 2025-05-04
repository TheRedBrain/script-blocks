package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
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
import me.fzzyhmstrs.lootables.api.LootablesApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptBlocks implements ModInitializer {
	public static final String MOD_ID = "scriptblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ServerConfig SERVER_CONFIG;

	// TODO DimLib MidnightLib Integration seems to be unstable, need to further investigate
//	public static final boolean isMidnightLibLoaded = FabricLoader.getInstance().isModLoaded("midnightlib");
	public static final boolean isLootablesLoaded = FabricLoader.getInstance().isModLoaded("lootables");

	public static boolean supplyLootableLoot(Identifier identifier, ServerPlayerEntity serverPlayerEntity, Vec3d pos, int rolls, int choices, boolean withChoice) {
		if (isLootablesLoaded) {
			if (withChoice) {
				return LootablesApi.supplyLootWithChoices(
						identifier,
						serverPlayerEntity,
						pos,
						(serverPlayerEntity1, vec3d) -> {

						},
						(serverPlayerEntity2, vec3d) -> {
							// gets called when player leaves choices screen without making a choice
							BlockEntity blockEntity = serverPlayerEntity.getWorld().getBlockEntity(new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
							if (blockEntity instanceof InteractiveLootBlockEntity interactiveLootBlockEntity) {
								interactiveLootBlockEntity.removePlayerFromSet(serverPlayerEntity);
							}
							if (blockEntity instanceof LootableVaultBlockEntity lootableVaultBlockEntity) {
								lootableVaultBlockEntity.unmarkAsRewarded(serverPlayerEntity);
							}
						},
						null,
						rolls,
						choices
				);
			} else {
				return LootablesApi.supplyLootRandomly(
						identifier,
						serverPlayerEntity,
						pos,
						null,
						rolls
				);
			}
		} else {
			info("Tried to supply loot via Lootables, but the mod is not installed!");
			return false;
		}
	}

	@Override
	public void onInitialize() {
		LOGGER.info("This was scripted!");

		// Config
		SERVER_CONFIG = ConfigApiJava.registerAndLoadConfig(ServerConfig::new, RegisterType.BOTH);

		// TODO DimLib MidnightLib Integration seems to be unstable, need to further investigate
//		if (isMidnightLibLoaded) {
//			DimensionAPI.suppressExperimentalWarning();
//			MidnightConfig.write(DimLibEntry.MODID);
//		}

		// Packets
		ServerPacketRegistry.init();

		// Registry
		ItemComponentRegistry.init();
		BlockRegistry.init();
		EntityRegistry.init();
		DimensionsManager.init();
		EventsRegistry.initializeEvents();
		CustomDynamicRegistries.init();
//		DialoguesRegistry.init();
//		DialogueAnswersRegistry.init();
//		ShopsRegistry.init();
//		BossesRegistry.init();
//		LocationsRegistry.init();
//		LootableVaultConfigsRegistry.init();
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