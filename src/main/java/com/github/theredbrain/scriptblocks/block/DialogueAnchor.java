package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface DialogueAnchor {

	BlockPos getDataBlockPos();

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

	static void openDialogueScreen(World world, MinecraftServer server, ServerPlayerEntity serverPlayerEntity, String dialogueIdentifierString, BlockPos dataBlockPos, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks) {
		Optional<RegistryEntry.Reference<Dialogue>> optionalDialogueReference = world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.tryParse(dialogueIdentifierString));

		if (optionalDialogueReference.isPresent()) {
			Dialogue dialogue = optionalDialogueReference.get().value();
			List<String> unlockedAnswersList = new ArrayList<>(List.of());
			List<String> visibleAnswersList = new ArrayList<>(List.of());
			PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
			ServerAdvancementLoader serverAdvancementLoader = server.getAdvancementLoader();

			for (String answerIdentifierString : dialogue.answerList()) {

				if (answerIdentifierString.isEmpty()) {
					continue;
				}

				DialogueAnswer dialogueAnswer = null;
				Optional<RegistryEntry.Reference<DialogueAnswer>> optionalDialogueAnswerReference1 = world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY).getEntry(Identifier.of(answerIdentifierString));
				if (optionalDialogueAnswerReference1.isPresent()) {
					dialogueAnswer = optionalDialogueAnswerReference1.get().value();
				}

				if (dialogueAnswer == null) {
					continue;
				}

				boolean isItemCostAffordable = true;
				if (!dialogueAnswer.availability().itemCosts().isEmpty()) {

					// player inventory copy
					int inventorySize = serverPlayerEntity.getInventory().size();
					Inventory playerInventoryCopy = new SimpleInventory(inventorySize);
					ItemStack itemStack;
					int j;
					for (int k = 0; k < inventorySize; k++) {
						playerInventoryCopy.setStack(k, serverPlayerEntity.getInventory().getStack(k).copy());
					}

					for (CommonDataStructures.ItemCost itemCost : dialogueAnswer.availability().itemCosts()) {
						ItemStack costStack = itemCost.itemStack();
						int itemCount = costStack.getCount();
						if (!costStack.isEmpty()) {
							for (j = 0; j < inventorySize; j++) {
								if (ItemStack.areItemsAndComponentsEqual(playerInventoryCopy.getStack(j), costStack)) {
									itemStack = playerInventoryCopy.getStack(j).copy();
									int stackCount = itemStack.getCount();
									if (stackCount >= itemCount) {
										itemStack.setCount(stackCount - itemCount);
										playerInventoryCopy.setStack(j, itemStack);
										itemCount = 0;
										break;
									} else {
										playerInventoryCopy.setStack(j, ItemStack.EMPTY);
										itemCount = itemCount - stackCount;
									}
								}
							}
							if (itemCount > 0) {
								isItemCostAffordable = false;
							}
						}
					}
				}

				CommonDataStructures.DataCheck unlockDataCheck = dialogueAnswer.availability().unlockDataCheck();
				CommonDataStructures.DataCheck lockDataCheck = dialogueAnswer.availability().lockDataCheck();

				boolean unlockDataCheckPassed = true;
				boolean lockDataCheckPassed = false;

				if (unlockDataCheck != CommonDataStructures.DataCheck.DEFAULT || lockDataCheck != CommonDataStructures.DataCheck.DEFAULT) {
					BlockEntity blockEntity = world.getBlockEntity(dataBlockPos);
					if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
						String existingUnlockData = providesDataBlockEntity.getData(unlockDataCheck.dataIdentifier());
						String existingLockData = providesDataBlockEntity.getData(lockDataCheck.dataIdentifier());

						unlockDataCheckPassed = switch (unlockDataCheck.comparisonMode()) {
							case 0 -> Objects.equals(unlockDataCheck.dataValue(), existingUnlockData);
							case 1 -> !Objects.equals(unlockDataCheck.dataValue(), existingUnlockData);
							case 2 ->
									ItemUtils.parseInt(unlockDataCheck.dataValue()) < ItemUtils.parseInt(existingUnlockData);
							case 3 ->
									ItemUtils.parseInt(unlockDataCheck.dataValue()) > ItemUtils.parseInt(existingUnlockData);
							default -> true;
						};
						lockDataCheckPassed = switch (lockDataCheck.comparisonMode()) {
							case 0 -> Objects.equals(lockDataCheck.dataValue(), existingLockData);
							case 1 -> !Objects.equals(lockDataCheck.dataValue(), existingLockData);
							case 2 ->
									ItemUtils.parseInt(lockDataCheck.dataValue()) < ItemUtils.parseInt(existingLockData);
							case 3 ->
									ItemUtils.parseInt(lockDataCheck.dataValue()) > ItemUtils.parseInt(existingLockData);
							default -> false;
						};
					}
				}

				if (playerAdvancementTracker != null && serverAdvancementLoader != null) {

					AdvancementEntry lockAdvancementEntry = serverAdvancementLoader.get(Identifier.of(dialogueAnswer.availability().lockAdvancement()));

					AdvancementEntry unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.of(dialogueAnswer.availability().unlockAdvancement()));

					if ((lockAdvancementEntry != null && !playerAdvancementTracker.getProgress(lockAdvancementEntry).isDone()) &&
							(unlockAdvancementEntry != null && playerAdvancementTracker.getProgress(unlockAdvancementEntry).isDone()) &&
							unlockDataCheckPassed && !lockDataCheckPassed
					) {
						if (isItemCostAffordable) {
							unlockedAnswersList.add(answerIdentifierString);
							visibleAnswersList.add(answerIdentifierString);
						} else if (dialogueAnswer.availability().showUnaffordableAnswer()) {
							visibleAnswersList.add(answerIdentifierString);
						}
					} else if (dialogueAnswer.availability().showLockedAnswer()) {
						visibleAnswersList.add(answerIdentifierString);
					}
				}
			}
			serverPlayerEntity.openHandledScreen(new ExtendedScreenHandlerFactory<>() {
				@Override
				public DialogueScreenHandler.DialogueData getScreenOpeningData(ServerPlayerEntity player) {
					return new DialogueScreenHandler.DialogueData(
							dialogueIdentifierString,
							dataBlockPos,
							dialogueUsedBlocks,
							dialogueTriggeredBlocks,
							unlockedAnswersList,
							visibleAnswersList
					);
				}

				@Override
				public Text getDisplayName() {
					return Text.empty();
				}

				@Nullable
				@Override
				public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
					return new DialogueScreenHandler(
							syncId,
							playerInventory,
							dialogueIdentifierString,
							dataBlockPos,
							dialogueUsedBlocks,
							dialogueTriggeredBlocks,
							unlockedAnswersList,
							visibleAnswersList
					);
				}
			});
		}
	}
}
