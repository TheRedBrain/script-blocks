package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.DialogueAnswersRegistry;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class DialogueAnswerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<DialogueAnswerPacket> {

	@Override
	public void receive(DialogueAnswerPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos dialogueBlockPos = payload.dialogueBlockPos();

		Identifier answerIdentifier = payload.answerIdentifier();

		DialogueAnswer dialogueAnswer = DialogueAnswersRegistry.getDialogueAnswer(answerIdentifier);

		MinecraftServer server = serverPlayerEntity.getServer();

		if (dialogueAnswer != null && server != null && serverPlayerEntity.getWorld().getBlockEntity(dialogueBlockPos) instanceof DialogueBlockEntity dialogueBlockEntity) {

			List<ItemUtils.VirtualItemStack> virtualItemStacks = dialogueAnswer.getItemCost();
			if (virtualItemStacks != null) {

				int playerInventorySize = serverPlayerEntity.getInventory().size();
				Inventory playerInventoryCopy = new SimpleInventory(playerInventorySize);
				ItemStack itemStack;

				for (int k = 0; k < playerInventorySize; k++) {
					playerInventoryCopy.setStack(k, serverPlayerEntity.getInventory().getStack(k).copy());
				}

				for (ItemUtils.VirtualItemStack ingredient : virtualItemStacks) {
					Item virtualItem = ItemUtils.getItemStackFromVirtualItemStack(ingredient).getItem();
					int ingredientCount = ingredient.getCount();

					for (int j = 0; j < playerInventorySize; j++) {
						if (playerInventoryCopy.getStack(j).isOf(virtualItem)) {
							itemStack = playerInventoryCopy.getStack(j).copy();
							int stackCount = itemStack.getCount();
							if (stackCount >= ingredientCount) {
								itemStack.setCount(stackCount - ingredientCount);
								playerInventoryCopy.setStack(j, itemStack);
								ingredientCount = 0;
								break;
							} else {
								playerInventoryCopy.setStack(j, ItemStack.EMPTY);
								ingredientCount = ingredientCount - stackCount;
							}
						}
					}
					if (ingredientCount > 0) {
						serverPlayerEntity.sendMessage(Text.translatable("gui.dialogue_screen.item_cost_too_high"));
						return;
					}
				}

				for (ItemUtils.VirtualItemStack ingredient : virtualItemStacks) {
					Item virtualItem = ItemUtils.getItemStackFromVirtualItemStack(ingredient).getItem();
					int ingredientCount = ingredient.getCount();

					for (int j = 0; j < playerInventorySize; j++) {
						if (serverPlayerEntity.getInventory().getStack(j).isOf(virtualItem)) {
							itemStack = serverPlayerEntity.getInventory().getStack(j).copy();
							int stackCount = itemStack.getCount();
							if (stackCount >= ingredientCount) {
								itemStack.setCount(stackCount - ingredientCount);
								serverPlayerEntity.getInventory().setStack(j, itemStack);
								ingredientCount = 0;
								break;
							} else {
								serverPlayerEntity.getInventory().setStack(j, ItemStack.EMPTY);
								ingredientCount = ingredientCount - stackCount;
							}
						}
					}
					if (ingredientCount > 0) {
						return;
					}
				}
			}

			// loot_table
			Identifier lootTableIdentifier = dialogueAnswer.getLootTable();
			if (lootTableIdentifier != null) {
				LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverPlayerEntity.getServerWorld()).add(LootContextParameters.THIS_ENTITY, serverPlayerEntity).add(LootContextParameters.ORIGIN, serverPlayerEntity.getPos()).build(LootContextTypes.ADVANCEMENT_REWARD);
				boolean bl = false;
				for (ItemStack itemStack : server.getLootManager().getLootTable(lootTableIdentifier).generateLoot(lootContextParameterSet)) {
					if (serverPlayerEntity.giveItemStack(itemStack)) {
						serverPlayerEntity.getWorld().playSound(null, serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((serverPlayerEntity.getRandom().nextFloat() - serverPlayerEntity.getRandom().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						bl = true;
						continue;
					}
					ItemEntity itemEntity = serverPlayerEntity.dropItem(itemStack, false);
					if (itemEntity == null) continue;
					itemEntity.resetPickupDelay();
					itemEntity.setOwner(serverPlayerEntity.getUuid());
				}
				if (bl) {
					serverPlayerEntity.currentScreenHandler.sendContentUpdates();
				}
			}

			// advancement
			Identifier advancementIdentifier = dialogueAnswer.getGrantedAdvancement();
			String criterionName = dialogueAnswer.getCriterionName();
			if (advancementIdentifier != null && criterionName != null) {
                AdvancementEntry advancementEntry = server.getAdvancementLoader().get(advancementIdentifier);
				if (advancementEntry != null) {
					serverPlayerEntity.getAdvancementTracker().grantCriterion(advancementEntry, criterionName);
				}
			}

			// overlay message
			String overlayMessage = dialogueAnswer.getOverlayMessage();
			if (overlayMessage != null) {
				serverPlayerEntity.sendMessageToClient(Text.translatable(overlayMessage), true);
			}

			String responseDialogueIdentifierString = dialogueAnswer.getResponseDialogue();
			if (responseDialogueIdentifierString.isEmpty()) {
				serverPlayerEntity.closeHandledScreen();
			} else {
				ServerPlayNetworking.send(serverPlayerEntity, new OpenDialogueScreenPacket(dialogueBlockPos, responseDialogueIdentifierString));
			}


			// trigger block
			String triggeredBlock = dialogueAnswer.getTriggeredBlock();
			if (triggeredBlock != null) {
				List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList = new ArrayList<>(List.of());
				List<String> keyList = new ArrayList<>(dialogueBlockEntity.getDialogueTriggeredBlocks().keySet());
				for (String key : keyList) {
					dialogueTriggeredBlocksList.add(new MutablePair<>(key, dialogueBlockEntity.getDialogueTriggeredBlocks().get(key)));
				}
				for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : dialogueTriggeredBlocksList) {
					if (entry.getLeft().equals(triggeredBlock)) {


						BlockEntity blockEntity = serverPlayerEntity.getWorld().getBlockEntity(entry.getRight().getLeft().add(dialogueBlockPos));

						if (blockEntity != dialogueBlockEntity) {
							boolean triggeredBlockResets = entry.getRight().getRight();
							if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
								resetable.reset();
							} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
								triggerable.trigger();
							}
						}
						break;
					}
				}
			}

			// use block
			String usedBlock = dialogueAnswer.getUsedBlock();
			if (usedBlock != null) {
				List<MutablePair<String, BlockPos>> dialogueUsedBlocksList = new ArrayList<>(List.of());
				List<String> keyList = new ArrayList<>(dialogueBlockEntity.getDialogueUsedBlocks().keySet());
				for (String key : keyList) {
					dialogueUsedBlocksList.add(new MutablePair<>(key, dialogueBlockEntity.getDialogueUsedBlocks().get(key)));
				}
				for (MutablePair<String, BlockPos> entry : dialogueUsedBlocksList) {
					if (entry.getLeft().equals(usedBlock)) {
						BlockHitResult blockHitResult = new BlockHitResult(serverPlayerEntity.getPos(), Direction.UP, entry.getRight().add(dialogueBlockPos), false);
						World world = serverPlayerEntity.getWorld();
						Hand hand = serverPlayerEntity.getActiveHand();
						ItemStack itemStack = serverPlayerEntity.getStackInHand(hand);

						serverPlayerEntity.interactionManager.interactBlock(serverPlayerEntity, world, itemStack, hand, blockHitResult);
					}
				}
			}
		}
	}
}
