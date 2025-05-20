package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.gui.screen.ingame.ShopScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredDispenserBlockScreen;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import com.github.theredbrain.scriptblocks.registry.ClientPacketRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.registry.KeyBindingsRegistry;
import com.github.theredbrain.scriptblocks.registry.ScreenHandlerTypesRegistry;
import com.github.theredbrain.scriptblocks.render.block.entity.AestheticDecoratedPotBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.BossControllerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.HousingBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.LootableVaultBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.MimicBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.PlayerDetectorBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.RelayTriggerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.StatusEffectApplierBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TeleporterBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredBeaconBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredDisplayBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredEntityRemoverBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.renderer.FakeVillagerEntityRenderer;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.api.RegisterType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

@Environment(value = EnvType.CLIENT)
public class ScriptBlocksClient implements ClientModInitializer {
//	public static ClientConfig CLIENT_CONFIG;

	@Override
	public void onInitializeClient() {
//		// Config
//		CLIENT_CONFIG = ConfigApiJava.registerAndLoadConfig(ClientConfig::new, RegisterType.CLIENT);

		// Packets
		ClientPacketRegistry.init();

		// Registry
		registerEntityRenderer();
		KeyBindingsRegistry.registerKeyBindings();
		registerTransparency();
		registerBlockEntityRenderer();
		registerScreens();
	}

	private void registerTransparency() {
		RenderLayer renderLayer = RenderLayer.getCutout();
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_IRON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_SPRUCE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_BIRCH_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_JUNGLE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_ACACIA_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_CHERRY_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_DARK_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_MANGROVE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_BAMBOO_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_CRIMSON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_WARPED_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_IRON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_SPRUCE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_BIRCH_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_JUNGLE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_ACACIA_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_CHERRY_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_DARK_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_MANGROVE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_BAMBOO_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_CRIMSON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TELEPORTER_WARPED_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_ANVIL, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_IRON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_SPRUCE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BIRCH_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_JUNGLE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_ACACIA_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CHERRY_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_DARK_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_MANGROVE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BAMBOO_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CRIMSON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_WARPED_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_IRON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_SPRUCE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BIRCH_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_JUNGLE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_ACACIA_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CHERRY_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_DARK_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_MANGROVE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_BAMBOO_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_CRIMSON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.USE_RELAY_WARPED_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TRIGGERED_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TRIGGERED_VILLAGER_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.BOSS_CONTROLLER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.LOOTABLE_VAULT_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(BlockRegistry.TRIGGERING_TRIAL_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getTranslucent(),
				BlockRegistry.AESTHETIC_NETHER_PORTAL
		);
	}

	private void registerBlockEntityRenderer() {
		BlockEntityRendererFactories.register(EntityRegistry.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, AestheticDecoratedPotBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.HOUSING_BLOCK_ENTITY, HousingBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.MIMIC_BLOCK_ENTITY, MimicBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.AREA_BLOCK_ENTITY, StatusEffectApplierBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.RELAY_TRIGGER_BLOCK_ENTITY, RelayTriggerBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.PLAYER_DETECTOR_BLOCK_ENTITY, PlayerDetectorBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.TELEPORTER_BLOCK_ENTITY, TeleporterBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.BOSS_CONTROLLER_BLOCK_ENTITY, BossControllerBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY, TriggeredEntityRemoverBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.TRIGGERED_BEACON_BLOCK_ENTITY, TriggeredBeaconBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.TRIGGERED_DISPLAY_BLOCK_ENTITY, TriggeredDisplayBlockEntityRenderer::new);
		BlockEntityRendererFactories.register(EntityRegistry.LOOTABLE_VAULT_BLOCK_ENTITY, LootableVaultBlockEntityRenderer::new);
	}

	private void registerScreens() {
//		HandledScreens.register(ScreenHandlerTypesRegistry.DIALOGUE_SCREEN_HANDLER, DialogueScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.TRIGGERED_DISPENSER_BLOCK_SCREEN_HANDLER, TriggeredDispenserBlockScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.SHOP_BLOCK_SCREEN_HANDLER, ShopScreen::new);
		HandledScreens.register(ScreenHandlerTypesRegistry.TELEPORTER_BLOCK_SCREEN_HANDLER, TeleporterBlockScreen::new);
	}

	private void registerEntityRenderer() {
		EntityRendererRegistry.register(EntityRegistry.FAKE_VILLAGER_ENTITY, FakeVillagerEntityRenderer::new);
	}
}