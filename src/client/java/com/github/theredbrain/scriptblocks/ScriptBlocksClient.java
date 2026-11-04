package com.github.theredbrain.scriptblocks;

import com.github.theredbrain.scriptblocks.gui.screen.ingame.DialogueScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.ShopScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredDispenserBlockScreen;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksClientEvents;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksKeyMappings;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksScreenHandlerTypes;
import com.github.theredbrain.scriptblocks.renderer.blockentity.AestheticDecoratedPotRenderer;
import com.github.theredbrain.scriptblocks.renderer.blockentity.BlockEntityWithBoundingBoxesRenderer;
import com.github.theredbrain.scriptblocks.renderer.blockentity.LootableVaultRenderer;
import com.github.theredbrain.scriptblocks.renderer.blockentity.UseRelayLecternRenderer;
import com.github.theredbrain.scriptblocks.renderer.entity.FakeVillagerEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(value = EnvType.CLIENT)
public class ScriptBlocksClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		ScriptBlocksClientEvents.initializeEvents();

		// Registry
		registerEntityRenderer();
		ScriptBlocksKeyMappings.registerKeyBindings();
		registerBlockEntityRenderer();
		registerScreens();
	}

	private void registerBlockEntityRenderer() {
		BlockEntityRenderers.register(ScriptBlocksEntities.AESTHETIC_DECORATED_POT_BLOCK_ENTITY, AestheticDecoratedPotRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.HOUSING_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
//		BlockEntityRenderers.register(ScriptBlocksEntities.MIMIC_BLOCK_ENTITY, MimicBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.AREA_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.AREA_FILLER_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.RELAY_TRIGGER_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.PLAYER_DETECTOR_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_DAMAGE_DEALING_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_BEACON_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
//		BlockEntityRenderers.register(ScriptBlocksEntities.TRIGGERED_DISPLAY_BLOCK_ENTITY, TriggeredDisplayBlockEntityRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.LOOTABLE_VAULT_BLOCK_ENTITY, LootableVaultRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TWO_AREAS_PLAYER_DETECTOR_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
		BlockEntityRenderers.register(ScriptBlocksEntities.USE_RELAY_LECTERN_BLOCK_ENTITY, UseRelayLecternRenderer::new);
		BlockEntityRenderers.register(ScriptBlocksEntities.TEAM_CONTROLLER_BLOCK_ENTITY, var0 -> new BlockEntityWithBoundingBoxesRenderer<>());
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