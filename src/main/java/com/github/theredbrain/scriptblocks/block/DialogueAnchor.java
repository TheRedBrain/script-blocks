package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.DialogueScreenHandler;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public interface DialogueAnchor {

	BlockPos getDataBlockPos();

	List<MutablePair<String, BlockPos>> getDialogueUsedBlocks();

	List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks();

	static String getDialogue(Level world, Player player, List<String> dialogueList) {
		if (dialogueList.isEmpty()) {
			return "";
		}
		PlayerAdvancements playerAdvancementTracker = null;
		ServerAdvancementManager serverAdvancementLoader = null;

		if (player instanceof ServerPlayer serverPlayerEntity) {
			playerAdvancementTracker = serverPlayerEntity.getAdvancements();
				MinecraftServer minecraftServer = serverPlayerEntity.level().getServer();
				serverAdvancementLoader = minecraftServer.getAdvancements();
		}
		String lockAdvancement;
		String unlockAdvancement;

		for (String dialogueEntry : dialogueList) {
			Optional<Holder.Reference<Dialogue>> optionalDialogueReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY).get().value().get(Identifier.tryParse(dialogueEntry));
			if (optionalDialogueReference.isPresent()) {
				lockAdvancement = optionalDialogueReference.get().value().lockAdvancement();
				unlockAdvancement = optionalDialogueReference.get().value().unlockAdvancement();

				AdvancementHolder lockAdvancementEntry = null;
				AdvancementHolder unlockAdvancementEntry = null;
				if (serverAdvancementLoader != null) {
					if (!lockAdvancement.isEmpty()) {
						lockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(lockAdvancement));
					}
					if (!unlockAdvancement.isEmpty()) {
						unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(unlockAdvancement));
					}

				}
				if (playerAdvancementTracker != null) {
					if ((lockAdvancement.isEmpty() || (lockAdvancementEntry != null && !playerAdvancementTracker.getOrStartProgress(lockAdvancementEntry).isDone())) && (unlockAdvancement.isEmpty() || (unlockAdvancementEntry != null && playerAdvancementTracker.getOrStartProgress(unlockAdvancementEntry).isDone()))) {
						return dialogueEntry;
					}
				}
			}
		}
		return "";
	}

	static void openDialogueScreen(Level world, MinecraftServer server, ServerPlayer serverPlayerEntity, String dialogueIdentifierString, BlockPos dataBlockPos, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks) {
		Optional<Holder.Reference<Dialogue>> optionalDialogueReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_REGISTRY_KEY).get().value().get(Identifier.tryParse(dialogueIdentifierString));

		if (optionalDialogueReference.isPresent()) {
			Dialogue dialogue = optionalDialogueReference.get().value();
			List<String> unlockedAnswersList = new ArrayList<>(List.of());
			List<String> visibleAnswersList = new ArrayList<>(List.of());
			PlayerAdvancements playerAdvancementTracker = serverPlayerEntity.getAdvancements();
			ServerAdvancementManager serverAdvancementLoader = server.getAdvancements();

			for (String answerIdentifierString : dialogue.answerList()) {

				if (!answerIdentifierString.isEmpty()) {

					DialogueAnswer dialogueAnswer = null;
					Optional<Holder.Reference<DialogueAnswer>> optionalDialogueAnswerReference1 = world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY).get().value().get(Identifier.parse(answerIdentifierString));
					if (optionalDialogueAnswerReference1.isPresent()) {
						dialogueAnswer = optionalDialogueAnswerReference1.get().value();
					}

					if (dialogueAnswer == null) {
						continue;
					}

					boolean isItemCostAffordable = true;
					if (!dialogueAnswer.availability().itemCosts().isEmpty()) {

						// player inventory copy
						int inventorySize = serverPlayerEntity.getInventory().getContainerSize();
						Container playerInventoryCopy = new SimpleContainer(inventorySize);
						ItemStack itemStack;
						int j;
						for (int k = 0; k < inventorySize; k++) {
							playerInventoryCopy.setItem(k, serverPlayerEntity.getInventory().getItem(k).copy());
						}

						for (CommonDataStructures.ItemCost itemCost : dialogueAnswer.availability().itemCosts()) {
							ItemStack costStack = itemCost.itemStack();
							int itemCount = costStack.getCount();
							if (!costStack.isEmpty()) {
								for (j = 0; j < inventorySize; j++) {
									if (ItemStack.isSameItemSameComponents(playerInventoryCopy.getItem(j), costStack)) {
										itemStack = playerInventoryCopy.getItem(j).copy();
										int stackCount = itemStack.getCount();
										if (stackCount >= itemCount) {
											itemStack.setCount(stackCount - itemCount);
											playerInventoryCopy.setItem(j, itemStack);
											itemCount = 0;
											break;
										} else {
											playerInventoryCopy.setItem(j, ItemStack.EMPTY);
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

						AdvancementHolder lockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(dialogueAnswer.availability().lockAdvancement()));

						AdvancementHolder unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.parse(dialogueAnswer.availability().unlockAdvancement()));

						if ((dialogueAnswer.availability().lockAdvancement().isEmpty() || (lockAdvancementEntry != null && !playerAdvancementTracker.getOrStartProgress(lockAdvancementEntry).isDone())) &&
								(dialogueAnswer.availability().unlockAdvancement().isEmpty() || (unlockAdvancementEntry != null && playerAdvancementTracker.getOrStartProgress(unlockAdvancementEntry).isDone())) &&
								unlockDataCheckPassed && !lockDataCheckPassed
						) {
							if (isItemCostAffordable) {
								unlockedAnswersList.add(answerIdentifierString);
								visibleAnswersList.add(answerIdentifierString);
							} else if (dialogueAnswer.availability().showUnaffordableAnswer()) {
								unlockedAnswersList.add("");
								visibleAnswersList.add(answerIdentifierString);
							}
						} else if (dialogueAnswer.availability().showLockedAnswer()) {
							unlockedAnswersList.add("");
							visibleAnswersList.add(answerIdentifierString);
						}
					}
				} else {
					unlockedAnswersList.add("");
					visibleAnswersList.add("");
				}
			}
			serverPlayerEntity.openMenu(createDialogueMenuProvider(
					Component.empty(),
					dialogueIdentifierString,
					dataBlockPos,
					dialogueUsedBlocks,
					dialogueTriggeredBlocks,
					unlockedAnswersList,
					visibleAnswersList
			));
		}
	}

	static MenuProvider createDialogueMenuProvider(
			Component title,
			String dialogueIdentifierString,
			BlockPos dataBlockPos,
			List<MutablePair<String, BlockPos>> dialogueUsedBlocks,
			List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks,
			List<String> unlockedAnswersList,
			List<String> visibleAnswersList
	) {
		return new ExtendedMenuProvider<>() {
			@Override
			public DialogueScreenHandler.DialogueData getScreenOpeningData(ServerPlayer player) {
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
			public Component getDisplayName() {
				return title;
			}

			@Nullable
			@Override
			public DialogueScreenHandler createMenu(int syncId, Inventory playerInventory, Player player) {
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
		};
	}

}
