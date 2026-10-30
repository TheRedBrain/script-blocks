package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.gui.screen.ingame.DialogueScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.ShopScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredDispenserBlockScreen;
import com.github.theredbrain.scriptblocks.registry.KeyBindingsRegistry;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksClientEvents;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.render.block.entity.AestheticDecoratedPotBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.AreaFillerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.HousingBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.LootableVaultBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.MimicBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.PlayerDetectorBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.RelayTriggerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.StatusEffectApplierBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TeamControllerBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredBeaconBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredDamageDealingBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredDisplayBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TriggeredEntityRemoverBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.TwoAreasPlayerDetectorBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.block.entity.UseRelayLecternBlockEntityRenderer;
import com.github.theredbrain.scriptblocks.render.renderer.FakeVillagerEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.rendertype.RenderType;

@Environment(value = EnvType.CLIENT)
public class ScriptBlocksClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ScriptBlocksClientEvents.initializeEvents();

		// Registry
		registerEntityRenderer();
		KeyBindingsRegistry.registerKeyBindings();
		registerTransparency();
		registerBlockEntityRenderer();
		registerScreens();
	}

	private void registerTransparency() {
		RenderType renderLayer = RenderType.getCutout();
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_IRON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SPRUCE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BIRCH_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_JUNGLE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_ACACIA_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CHERRY_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_DARK_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_MANGROVE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BAMBOO_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CRIMSON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WARPED_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_EXPOSED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WEATHERED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OXIDIZED_COPPER_DOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_IRON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SPRUCE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BIRCH_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_JUNGLE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_ACACIA_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CHERRY_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_DARK_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_MANGROVE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BAMBOO_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CRIMSON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WARPED_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_EXPOSED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WEATHERED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OXIDIZED_COPPER_TRAPDOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_IRON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_SPRUCE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_BIRCH_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_JUNGLE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_ACACIA_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_CHERRY_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_DARK_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_MANGROVE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_BAMBOO_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_CRIMSON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_WARPED_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_EXPOSED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_WEATHERED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_OXIDIZED_COPPER_DOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_IRON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_SPRUCE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_BIRCH_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_JUNGLE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_ACACIA_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_CHERRY_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_DARK_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_MANGROVE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_BAMBOO_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_CRIMSON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_WARPED_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_EXPOSED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_WEATHERED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TELEPORTER_OXIDIZED_COPPER_TRAPDOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_ANVIL, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_IRON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_SPRUCE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_BIRCH_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_JUNGLE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_ACACIA_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_CHERRY_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_DARK_OAK_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_MANGROVE_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_BAMBOO_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_CRIMSON_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_WARPED_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_EXPOSED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_WEATHERED_COPPER_DOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_OXIDIZED_COPPER_DOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_IRON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_SPRUCE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_BIRCH_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_JUNGLE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_ACACIA_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_CHERRY_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_DARK_OAK_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_MANGROVE_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_BAMBOO_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_CRIMSON_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_WARPED_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_EXPOSED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_WEATHERED_COPPER_TRAPDOOR, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.USE_RELAY_OXIDIZED_COPPER_TRAPDOOR, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_TORCH, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WALL_TORCH, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SOUL_TORCH, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SOUL_WALL_TORCH, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_LANTERN, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SOUL_LANTERN, renderLayer);

		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TRIGGERED_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TRIGGERED_VILLAGER_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.BOSS_CONTROLLER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.LOOTABLE_VAULT_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlock(ScriptBlocksBlocks.TRIGGERING_TRIAL_SPAWNER_BLOCK, renderLayer);
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.getTranslucent(),
				ScriptBlocksBlocks.AESTHETIC_NETHER_PORTAL
		);
	}

	private void registerBlockEntityRenderer() {
		BlockEntityRenderers.register(ScriptBlocksEntities.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, AestheticDecoratedPotBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.HOUSING_BLOCK_ENTITY, HousingBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.MIMIC_BLOCK_ENTITY, MimicBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.AREA_BLOCK_ENTITY, StatusEffectApplierBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.AREA_FILLER_BLOCK_ENTITY, AreaFillerBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.RELAY_TRIGGER_BLOCK_ENTITY, RelayTriggerBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.PLAYER_DETECTOR_BLOCK_ENTITY, PlayerDetectorBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_DAMAGE_DEALING_BLOCK_ENTITY, TriggeredDamageDealingBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY, TriggeredEntityRemoverBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_BEACON_BLOCK_ENTITY, TriggeredBeaconBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_DISPLAY_BLOCK_ENTITY, TriggeredDisplayBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.LOOTABLE_VAULT_BLOCK_ENTITY, LootableVaultBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TWO_AREAS_PLAYER_DETECTOR_BLOCK_ENTITY, TwoAreasPlayerDetectorBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.USE_RELAY_LECTERN_BLOCK_ENTITY, UseRelayLecternBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TEAM_CONTROLLER_BLOCK_ENTITY, TeamControllerBlockEntityRenderer::new);
	}

	private void registerScreens() {
		MenuScreens.register(ScriptBlocksScreenHandlerTypes.DIALOGUE_SCREEN_HANDLER, DialogueScreen::new);
		MenuScreens.register(ScriptBlocksScreenHandlerTypes.TRIGGERED_DISPENSER_BLOCK_SCREEN_HANDLER, TriggeredDispenserBlockScreen::new);
		MenuScreens.register(ScriptBlocksScreenHandlerTypes.SHOP_BLOCK_SCREEN_HANDLER, ShopScreen::new);
		MenuScreens.register(ScriptBlocksScreenHandlerTypes.TELEPORTER_BLOCK_SCREEN_HANDLER, TeleporterBlockScreen::new);
	}

	private void registerEntityRenderer() {
		EntityRendererRegistry.register(ScriptBlocksEntities.FAKE_VILLAGER_ENTITY, FakeVillagerEntityRenderer::new);
	}
}