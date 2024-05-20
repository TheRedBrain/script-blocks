package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.screen.DialogueBlockScreenHandler;
import com.github.theredbrain.scriptblocks.screen.ShopBlockScreenHandler;
import com.github.theredbrain.scriptblocks.screen.TeleporterBlockScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;

public class ScreenHandlerTypesRegistry {
    public static final ScreenHandlerType<DialogueBlockScreenHandler> DIALOGUE_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(DialogueBlockScreenHandler::new);
    public static final ScreenHandlerType<ShopBlockScreenHandler> SHOP_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(ShopBlockScreenHandler::new);
    public static final ScreenHandlerType<TeleporterBlockScreenHandler> TELEPORTER_BLOCK_SCREEN_HANDLER = new ExtendedScreenHandlerType<>(TeleporterBlockScreenHandler::new);

    public static void registerAll() {
        Registry.register(Registries.SCREEN_HANDLER, ScriptBlocksMod.identifier("dialogue"), DIALOGUE_BLOCK_SCREEN_HANDLER);
        Registry.register(Registries.SCREEN_HANDLER, ScriptBlocksMod.identifier("shop"), SHOP_BLOCK_SCREEN_HANDLER);
        Registry.register(Registries.SCREEN_HANDLER, ScriptBlocksMod.identifier("teleporter"), TELEPORTER_BLOCK_SCREEN_HANDLER);
    }
}
