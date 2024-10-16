package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.github.theredbrain.scriptblocks.config.ServerConfigWrapper;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.github.theredbrain.scriptblocks.registry.BossesRegistry;
import com.github.theredbrain.scriptblocks.registry.DialogueAnswersRegistry;
import com.github.theredbrain.scriptblocks.registry.DialoguesRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.EventsRegistry;
import com.github.theredbrain.scriptblocks.registry.GameRulesRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemComponentRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemGroupRegistry;
import com.github.theredbrain.scriptblocks.registry.ItemRegistry;
import com.github.theredbrain.scriptblocks.registry.LocationsRegistry;
import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import com.github.theredbrain.scriptblocks.registry.ServerPacketRegistry;
import com.github.theredbrain.scriptblocks.registry.ShopsRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.github.theredbrain.scriptblocks.registry.StructurePlacementTypesRegistry;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptBlocks implements ModInitializer {
	public static final String MOD_ID = "scriptblocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static ServerConfig serverConfig;

	@Override
	public void onInitialize() {
		LOGGER.info("This was scripted!");

		// Config
		AutoConfig.register(ServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
		serverConfig = ((ServerConfigWrapper) AutoConfig.getConfigHolder(ServerConfigWrapper.class).getConfig()).server;

		// Packets
		ServerPacketRegistry.init();

		// Registry
		ItemComponentRegistry.init();
		BlockRegistry.init();
		EntityRegistry.init();
		DimensionsManager.init();
		EventsRegistry.initializeEvents();
		DialoguesRegistry.init();
		DialogueAnswersRegistry.init();
		ShopsRegistry.init();
		BossesRegistry.init();
		LocationsRegistry.init();
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