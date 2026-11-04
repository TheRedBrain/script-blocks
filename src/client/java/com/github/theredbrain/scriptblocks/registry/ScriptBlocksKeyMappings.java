package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

@Environment(value = EnvType.CLIENT)
public class ScriptBlocksKeyMappings {

	public static KeyMapping openHousingScreen;
	public static boolean openHousingScreenBoolean;

	public static void registerKeyBindings() {
		ScriptBlocksKeyMappings.openHousingScreen = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.scriptblocks.housingScreen",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_H,
				KeyMapping.Category.GAMEPLAY
		));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (ScriptBlocksKeyMappings.openHousingScreen.consumeClick()) {
				if (!openHousingScreenBoolean) {
					openHousingScreen(client);
				}
				openHousingScreenBoolean = true;
			} else if (openHousingScreenBoolean) {
				openHousingScreenBoolean = false;
			}
		});
	}

	public static void openHousingScreen(Minecraft client) {
		if (client.player != null) {
			((DuckPlayerEntityMixin) client.player).scriptblocks$openHousingScreen();
		}
	}
}
