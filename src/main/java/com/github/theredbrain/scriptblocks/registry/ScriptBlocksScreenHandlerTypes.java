package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import com.github.theredbrain.scriptblocks.screen.TriggeredDispenserBlockScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ScriptBlocksScreenHandlerTypes {
	public static final MenuType<TriggeredDispenserBlockScreenHandler> TRIGGERED_DISPENSER_BLOCK_SCREEN_HANDLER = new MenuType<>(TriggeredDispenserBlockScreenHandler::new, FeatureFlags.VANILLA_SET);
	public static final MenuType<DialogueScreenHandler> DIALOGUE_SCREEN_HANDLER = new ExtendedMenuType<>(DialogueScreenHandler::new, DialogueScreenHandler.DialogueData.PACKET_CODEC);
	public static final MenuType<ShopScreenHandler> SHOP_BLOCK_SCREEN_HANDLER = new ExtendedMenuType<>(ShopScreenHandler::new, ShopScreenHandler.ShopBlockData.PACKET_CODEC);
	public static final MenuType<TeleporterBlockScreenHandler> TELEPORTER_BLOCK_SCREEN_HANDLER = new ExtendedMenuType<>(TeleporterBlockScreenHandler::new, TeleporterBlockScreenHandler.TeleporterBlockData.PACKET_CODEC);

	public static void init() {
		Registry.register(BuiltInRegistries.MENU, ScriptBlocks.identifier("triggered_dispenser"), TRIGGERED_DISPENSER_BLOCK_SCREEN_HANDLER);
		Registry.register(BuiltInRegistries.MENU, ScriptBlocks.identifier("dialogue"), DIALOGUE_SCREEN_HANDLER);
		Registry.register(BuiltInRegistries.MENU, ScriptBlocks.identifier("shop"), SHOP_BLOCK_SCREEN_HANDLER);
		Registry.register(BuiltInRegistries.MENU, ScriptBlocks.identifier("teleporter"), TELEPORTER_BLOCK_SCREEN_HANDLER);
	}
}
