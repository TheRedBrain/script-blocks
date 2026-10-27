package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class DebuggingHelper {

	public static void sendBossControllerLogMessage(String message, @Nullable Player playerEntity) {
		if (ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_logging && ScriptBlocksConfigs.SERVER_CONFIG.enable_boss_controller_debugging) {
			sendDebuggingMessage(message, playerEntity);
		}
	}

	public static void sendLootableVaultLogMessage(String message, @Nullable Player playerEntity) {
		if (ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_logging && ScriptBlocksConfigs.SERVER_CONFIG.enable_lootable_vault_debugging) {
			sendDebuggingMessage(message, playerEntity);
		}
	}

	public static void sendJigsawPlacerLogMessage(String message, @Nullable Player playerEntity) {
		if (ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_logging && ScriptBlocksConfigs.SERVER_CONFIG.enable_jigsaw_placer_debugging) {
			sendDebuggingMessage(message, playerEntity);
		}
	}

	public static boolean isTeleporterLoggingEnabled() {
		return ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_logging && ScriptBlocksConfigs.SERVER_CONFIG.enable_teleporter_debugging;
	}

	public static boolean isRegistryLoggingEnabled() {
		return ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_logging && ScriptBlocksConfigs.SERVER_CONFIG.enable_registry_debugging;
	}

	public static void sendDebuggingMessage(String message, @Nullable Player playerEntity) {
		if (ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_console_logging) {
			ScriptBlocks.LOGGER.info("[" + ScriptBlocks.MOD_ID + "] [info]: " + message);
		}
		if (ScriptBlocksConfigs.SERVER_CONFIG.enable_debug_messages && playerEntity != null) {
			playerEntity.displayClientMessage(Component.nullToEmpty("[" + ScriptBlocks.MOD_ID + "] [info]: " + message));
		}
	}
}
