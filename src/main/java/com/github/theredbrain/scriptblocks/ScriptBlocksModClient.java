package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.client.gui.screen.ingame.DialogueBlockScreen;
import com.github.theredbrain.scriptblocks.client.gui.screen.ingame.ShopBlockScreen;
import com.github.theredbrain.scriptblocks.client.gui.screen.ingame.TeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.client.render.block.entity.*;
import com.github.theredbrain.scriptblocks.client.render.renderer.SpawnerBoundEntityRenderer;
import com.github.theredbrain.scriptblocks.client.render.renderer.SpawnerBoundVillagerEntityRenderer;
import com.github.theredbrain.scriptblocks.config.ClientConfig;
import com.github.theredbrain.scriptblocks.config.ClientConfigWrapper;
import com.github.theredbrain.scriptblocks.registry.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

public class ScriptBlocksModClient implements ClientModInitializer {
	public static ClientConfig clientConfig;
	@Override
	public void onInitializeClient() {
		// Config
		AutoConfig.register(ClientConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
		clientConfig = ((ClientConfigWrapper)AutoConfig.getConfigHolder(ClientConfigWrapper.class).getConfig()).client;

		// Packets
		ClientPacketRegistry.init();

		// Registry
		KeyBindingsRegistry.registerKeyBindings();
		registerTransparency();
		registerBlockEntityRenderer();
		registerEntityRenderer();
		registerScreens();
		EventsRegistry.initializeClientEvents();
	}

	private void registerTransparency() {
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_OAK_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_IRON_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_SPRUCE_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BIRCH_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_JUNGLE_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_ACACIA_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CHERRY_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_DARK_OAK_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_MANGROVE_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BAMBOO_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CRIMSON_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_WARPED_DOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_OAK_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_IRON_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_SPRUCE_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BIRCH_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_JUNGLE_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_ACACIA_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CHERRY_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_DARK_OAK_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_MANGROVE_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BAMBOO_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CRIMSON_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_WARPED_TRAPDOOR, RenderLayer.getCutout());
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TRIGGERED_SPAWNER_BLOCK, RenderLayer.getCutout());
	}

	private void registerBlockEntityRenderer() {
		BlockEntityRendererFactories.register(EntityRegistry.HOUSING_BLOCK_ENTITY, HousingBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.MIMIC_BLOCK_ENTITY, MimicBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.AREA_BLOCK_ENTITY, StatusEffectApplierBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.RELAY_TRIGGER_BLOCK_ENTITY, RelayTriggerBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.TELEPORTER_BLOCK_ENTITY, TeleporterBlockEntityRenderer::new);
	}

	private void registerEntityRenderer() {
		EntityRendererRegistry.register(EntityRegistry.SPAWNER_BOUND_ENTITY, SpawnerBoundEntityRenderer::new);
		EntityRendererRegistry.register(EntityRegistry.SPAWNER_BOUND_VILLAGER_ENTITY, SpawnerBoundVillagerEntityRenderer::new);
	}

	private void registerScreens() {
		HandledScreens.register(ScreenHandlerTypesRegistry.DIALOGUE_BLOCK_SCREEN_HANDLER, DialogueBlockScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.SHOP_BLOCK_SCREEN_HANDLER, ShopBlockScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.TELEPORTER_BLOCK_SCREEN_HANDLER, TeleporterBlockScreen::new);
	}
}