package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class DebuggingHelper {

	public static boolean isTeleporterLoggingEnabled() {
		return ScriptBlocks.serverConfig.enable_debug_logging && ScriptBlocks.serverConfig.enable_teleporter_debugging;
	}

	public static void sendDebuggingMessage(String message, @Nullable PlayerEntity playerEntity) {
		if (ScriptBlocks.serverConfig.enable_debug_console_logging) {
			ScriptBlocks.LOGGER.info("[" + ScriptBlocks.MOD_ID + "] [info]: " + message);
		}
		if (ScriptBlocks.serverConfig.enable_debug_messages && playerEntity != null) {
			playerEntity.sendMessage(Text.of("[" + ScriptBlocks.MOD_ID + "] [info]: " + message));
		}
	}
}
