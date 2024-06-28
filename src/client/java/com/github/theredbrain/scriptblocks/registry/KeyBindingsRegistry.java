package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class KeyBindingsRegistry {

	public static KeyBinding openHousingScreen;
	public static boolean openHousingScreenBoolean;

	public static void registerKeyBindings() {
		KeyBindingsRegistry.openHousingScreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.scriptblocks.housingScreen",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				"category.scriptblocks.category"
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (KeyBindingsRegistry.openHousingScreen.wasPressed()) {
				if (!openHousingScreenBoolean) {
					openHousingScreen(client);
				}
				openHousingScreenBoolean = true;
			} else if (openHousingScreenBoolean) {
				openHousingScreenBoolean = false;
			}
		});
	}

	public static void openHousingScreen(MinecraftClient client) {
		if (client.player != null) {
			((DuckPlayerEntityMixin) client.player).scriptblocks$openHousingScreen();
		}
	}
}
