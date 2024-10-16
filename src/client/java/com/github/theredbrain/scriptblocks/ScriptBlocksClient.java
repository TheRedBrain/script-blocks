package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.config.ClientConfig;
import com.github.theredbrain.scriptblocks.config.ClientConfigWrapper;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.ShopScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.github.theredbrain.scriptblocks.registry.ClientPacketRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.KeyBindingsRegistry;
import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import com.github.theredbrain.scriptblocks.render.block.entity.HousingBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.MimicBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.RelayTriggerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.StatusEffectApplierBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TeleporterBlockEntityRenderer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(value = EnvType.CLIENT)
public class ScriptBlocksClient implements ClientModInitializer {
	public static ClientConfig clientConfig;

	@Override
	public void onInitializeClient() {
		// Config
		AutoConfig.register(ClientConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
		clientConfig = ((ClientConfigWrapper) AutoConfig.getConfigHolder(ClientConfigWrapper.class).getConfig()).client;

		// Packets
		ClientPacketRegistry.init();

		// Registry
		KeyBindingsRegistry.registerKeyBindings();
		registerTransparency();
		registerBlockEntityRenderer();
		registerScreens();
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

	private void registerScreens() {
//		HandledScreens.register(ScreenHandlerTypesRegistry.DIALOGUE_SCREEN_HANDLER, DialogueScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.SHOP_BLOCK_SCREEN_HANDLER, ShopScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.TELEPORTER_BLOCK_SCREEN_HANDLER, TeleporterBlockScreen::new);
	}
}