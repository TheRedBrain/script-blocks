package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import java.util.Optional;

public class DialogueAnswerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<DialogueAnswerPacket> {

	@Override
	public void receive(DialogueAnswerPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		Identifier answerIdentifier = payload.answerIdentifier();
		BlockPos dataBlockPos = payload.dataBlockPos();
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = payload.dialogueUsedBlocks();
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = payload.dialogueTriggeredBlocks();

		Level world = serverPlayerEntity.level();
		Optional<Holder.Reference<DialogueAnswer>> optionalDialogueAnswerReference = world.registryAccess().get(ScriptBlocksDynamicRegistries.DIALOGUE_ANSWER_REGISTRY_KEY).get().value().get(answerIdentifier);

		MinecraftServer server = serverPlayerEntity.level().getServer();

		if (optionalDialogueAnswerReference.isPresent()) {
			DialogueAnswer dialogueAnswer = optionalDialogueAnswerReference.get().value();

			if (!dialogueAnswer.availability().itemCosts().isEmpty()) {

				// create inventory copy
				int playerInventorySize = serverPlayerEntity.getInventory().getContainerSize();
				Container playerInventoryCopy = new SimpleContainer(playerInventorySize);
				ItemStack itemStack;
				for (int k = 0; k < playerInventorySize; k++) {
					playerInventoryCopy.setItem(k, serverPlayerEntity.getInventory().getItem(k).copy());
				}

				// check for itemCosts
				for (CommonDataStructures.ItemCost itemCost : dialogueAnswer.availability().itemCosts()) {
					int itemCount = itemCost.itemStack().getCount();
					for (int j = 0; j < playerInventorySize; j++) {

						if (ItemStack.isSameItemSameComponents(playerInventoryCopy.getItem(j), itemCost.itemStack())) {
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
						serverPlayerEntity.sendSystemMessage(Component.translatable("gui.dialogue_screen.item_cost_too_high"));
						return;
					}
				}

				// apply item cost
				for (CommonDataStructures.ItemCost itemCost : dialogueAnswer.availability().itemCosts()) {

					if (itemCost.consumeStack()) {
						int ingredientCount = itemCost.itemStack().getCount();
						for (int j = 0; j < playerInventorySize; j++) {

							if (ItemStack.isSameItemSameComponents(serverPlayerEntity.getInventory().getItem(j), itemCost.itemStack())) {
								itemStack = serverPlayerEntity.getInventory().getItem(j).copy();
								int stackCount = itemStack.getCount();

								if (stackCount >= ingredientCount) {
									itemStack.setCount(stackCount - ingredientCount);
									serverPlayerEntity.getInventory().setItem(j, itemStack);
									ingredientCount = 0;
									break;
								} else {
									serverPlayerEntity.getInventory().setItem(j, ItemStack.EMPTY);
									ingredientCount = ingredientCount - stackCount;
								}
							}
						}
						if (ingredientCount > 0) {
							return;
						}
					}
				}
			}

			// loot_table
			String lootTable = dialogueAnswer.results().lootTable();

			if (!lootTable.isEmpty()) {
				Identifier lootTableIdentifier = Identifier.parse(lootTable);
				LootParams params = new LootParams.Builder(serverPlayerEntity.level()).withParameter(LootContextParams.THIS_ENTITY, serverPlayerEntity).withParameter(LootContextParams.ORIGIN, serverPlayerEntity.position()).create(LootContextParamSets.ADVANCEMENT_REWARD);
				boolean bl = false;
				for (ItemStack itemStack : server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, lootTableIdentifier)).getRandomItems(params)) {

					if (serverPlayerEntity.addItem(itemStack)) {
						serverPlayerEntity.level().playSound(null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						bl = true;
						continue;
					}
					ItemEntity itemEntity = serverPlayerEntity.drop(itemStack, false);

					if (itemEntity == null) continue;
					itemEntity.setNoPickUpDelay();
					itemEntity.setTarget(serverPlayerEntity.getUUID());
				}
				if (bl) {
					serverPlayerEntity.containerMenu.broadcastChanges();
				}
			}

			// advancement
			String grantedAdvancement = dialogueAnswer.results().grantedAdvancement();
			String criterionName = dialogueAnswer.results().criterionName();

			if (!grantedAdvancement.isEmpty() && !criterionName.isEmpty()) {
				AdvancementHolder advancementEntry = server.getAdvancements().get(Identifier.parse(grantedAdvancement));

				if (advancementEntry != null) {
					serverPlayerEntity.getAdvancements().award(advancementEntry, criterionName);
				}
			}

			// overlay message
			String overlayMessage = dialogueAnswer.results().overlayMessage();

			if (overlayMessage != null) {
				serverPlayerEntity.sendSystemMessage(Component.translatable(overlayMessage), true);
			}

			String responseDialogueIdentifierString = DialogueAnchor.getDialogue(serverPlayerEntity.level(), serverPlayerEntity, dialogueAnswer.responseDialogues());

			if (responseDialogueIdentifierString.isEmpty()) {
				serverPlayerEntity.closeContainer();
			} else {
				DialogueAnchor.openDialogueScreen(world, server, serverPlayerEntity, responseDialogueIdentifierString, dataBlockPos, dialogueUsedBlocks, dialogueTriggeredBlocks);
			}


			// trigger block
			String triggeredBlock = dialogueAnswer.results().triggeredBlock();

			if (triggeredBlock != null) {
				for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : dialogueTriggeredBlocks) {

					if (entry.getLeft().equals(triggeredBlock)) {
						BlockEntity blockEntity = serverPlayerEntity.level().getBlockEntity(entry.getRight().getLeft());
						boolean triggeredBlockResets = entry.getRight().getRight();

						if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
							resetable.reset();
						} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
							triggerable.trigger();
						}
						break;
					}
				}
			}

			// use block
			String usedBlock = dialogueAnswer.results().usedBlock();

			if (usedBlock != null) {
				for (MutablePair<String, BlockPos> entry : dialogueUsedBlocks) {

					if (entry.getLeft().equals(usedBlock)) {
						BlockHitResult blockHitResult = new BlockHitResult(serverPlayerEntity.position(), Direction.UP, entry.getRight(), false);
						InteractionHand hand = serverPlayerEntity.getUsedItemHand();
						ItemStack itemStack = serverPlayerEntity.getItemInHand(hand);

						serverPlayerEntity.gameMode.useItemOn(serverPlayerEntity, world, itemStack, hand, blockHitResult);
					}
				}
			}
		}
	}
}
