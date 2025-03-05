package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DebuggingHelper {

	public static void sendBossControllerLogMessage(String message, @Nullable PlayerEntity playerEntity) {
		if (ScriptBlocks.SERVER_CONFIG.enable_debug_logging && ScriptBlocks.SERVER_CONFIG.enable_boss_controller_debugging) {
			sendDebuggingMessage(message, playerEntity);
		}
	}

	public static boolean isTeleporterLoggingEnabled() {
		return ScriptBlocks.SERVER_CONFIG.enable_debug_logging && ScriptBlocks.SERVER_CONFIG.enable_teleporter_debugging;
	}

	public static boolean isRegistryLoggingEnabled() {
		return ScriptBlocks.SERVER_CONFIG.enable_debug_logging && ScriptBlocks.SERVER_CONFIG.enable_registry_debugging;
	}

	public static void sendDebuggingMessage(String message, @Nullable PlayerEntity playerEntity) {
		if (ScriptBlocks.SERVER_CONFIG.enable_debug_console_logging) {
			ScriptBlocks.LOGGER.info("[" + ScriptBlocks.MOD_ID + "] [info]: " + message);
		}
		if (ScriptBlocks.SERVER_CONFIG.enable_debug_messages && playerEntity != null) {
			playerEntity.sendMessage(Text.of("[" + ScriptBlocks.MOD_ID + "] [info]: " + message));
		}
	}
}
