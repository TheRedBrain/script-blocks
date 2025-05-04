package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.lang.ref.Reference;
import java.util.List;
import java.util.Optional;

public interface DialogueAnchor {
	List<MutablePair<String, BlockPos>> getDialogueUsedBlocks();

	List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks();

	static String getDialogue(World world, PlayerEntity player, List<String> dialogueList) {
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
			Optional<RegistryEntry.Reference<Dialogue>> optionalDialogueReference = world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(dialogueEntry));
			if (optionalDialogueReference.isPresent()) {
				lockAdvancement = optionalDialogueReference.get().value().lockAdvancement();
				unlockAdvancement = optionalDialogueReference.get().value().unlockAdvancement();

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
