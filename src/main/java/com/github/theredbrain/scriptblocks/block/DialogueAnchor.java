package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.registry.DialoguesRegistry;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public interface DialogueAnchor {
	List<MutablePair<String, BlockPos>> getDialogueUsedBlocks();

	List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks();

	static String getDialogue(PlayerEntity player, List<String> dialogueList) {
		if (dialogueList.isEmpty()) {
			return "";
		}
		PlayerAdvancementTracker playerAdvancementTracker = null;
		ServerAdvancementLoader serverAdvancementLoader = null;

		if (player instanceof ServerPlayerEntity serverPlayerEntity) {
			playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
			MinecraftServer minecraftServer = serverPlayerEntity.getServer();
			if (minecraftServer != null) {
				serverAdvancementLoader = minecraftServer.getAdvancementLoader();
			}
		}
		String lockAdvancement;
		String unlockAdvancement;

		for (String dialogueEntry : dialogueList) {
			Dialogue dialogue = DialoguesRegistry.registeredDialogues.get(Identifier.tryParse(dialogueEntry));
			if (dialogue != null) {
				lockAdvancement = dialogue.lockAdvancement();
				unlockAdvancement = dialogue.unlockAdvancement();

				AdvancementEntry lockAdvancementEntry = null;
				AdvancementEntry unlockAdvancementEntry = null;
				if (serverAdvancementLoader != null) {
					if (!lockAdvancement.isEmpty()) {
						lockAdvancementEntry = serverAdvancementLoader.get(Identifier.of(lockAdvancement));
					}
					if (!unlockAdvancement.isEmpty()) {
						unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.of(unlockAdvancement));
					}

				}
				if (playerAdvancementTracker != null) {
					if ((lockAdvancement.isEmpty() || (lockAdvancementEntry != null && !playerAdvancementTracker.getProgress(lockAdvancementEntry).isDone())) && (unlockAdvancement.isEmpty() || (unlockAdvancementEntry != null && playerAdvancementTracker.getProgress(unlockAdvancementEntry).isDone()))) {
						return dialogueEntry;
					}
				}
			}
		}
		return "";
	}

}
