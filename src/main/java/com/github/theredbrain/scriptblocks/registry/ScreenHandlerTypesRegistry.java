package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerTypesRegistry {
	public static final ScreenHandlerType<ShopScreenHandler> SHOP_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(ShopScreenHandler::new, ShopScreenHandler.ShopBlockData.PACKET_CODEC);
	public static final ScreenHandlerType<TeleporterBlockScreenHandler> TELEPORTER_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(TeleporterBlockScreenHandler::new, TeleporterBlockScreenHandler.TeleporterBlockData.PACKET_CODEC);

	public static void registerAll() {
		Registry.register(Registries.SCREEN_HANDLER, ScriptBlocks.identifier("shop"), SHOP_BLOCK_SCREEN_HANDLER);
		Registry.register(Registries.SCREEN_HANDLER, ScriptBlocks.identifier("teleporter"), TELEPORTER_BLOCK_SCREEN_HANDLER);
	}
}
