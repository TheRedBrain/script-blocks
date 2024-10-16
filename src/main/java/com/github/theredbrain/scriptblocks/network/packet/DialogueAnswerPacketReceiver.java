package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.registry.DialogueAnswersRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

import java.util.List;

public class DialogueAnswerPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<DialogueAnswerPacket> {

	@Override
	public void receive(DialogueAnswerPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		Identifier answerIdentifier = payload.answerIdentifier();
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = payload.dialogueUsedBlocks();
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = payload.dialogueTriggeredBlocks();

		DialogueAnswer dialogueAnswer = DialogueAnswersRegistry.registeredDialogueAnswers.get(answerIdentifier);

		MinecraftServer server = serverPlayerEntity.getServer();

		if (dialogueAnswer != null && server != null) {

			List<ItemStack> virtualItemStacks = dialogueAnswer.itemCost();
			if (virtualItemStacks != null) {

				int playerInventorySize = serverPlayerEntity.getInventory().size();
				Inventory playerInventoryCopy = new SimpleInventory(playerInventorySize);
				ItemStack itemStack;

				for (int k = 0; k < playerInventorySize; k++) {
					playerInventoryCopy.setStack(k, serverPlayerEntity.getInventory().getStack(k).copy());
				}

				for (ItemStack ingredient : virtualItemStacks) {
					Item virtualItem = ingredient.getItem();
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

				for (ItemStack ingredient : virtualItemStacks) {
					Item virtualItem = ingredient.getItem();
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
			Identifier lootTableIdentifier = dialogueAnswer.lootTable();
			if (lootTableIdentifier != null) {
				LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder(serverPlayerEntity.getServerWorld()).add(LootContextParameters.THIS_ENTITY, serverPlayerEntity).add(LootContextParameters.ORIGIN, serverPlayerEntity.getPos()).build(LootContextTypes.ADVANCEMENT_REWARD);
				boolean bl = false;
				for (ItemStack itemStack : server.getReloadableRegistries().getLootTable(RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableIdentifier)).generateLoot(lootContextParameterSet)) {
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
			Identifier advancementIdentifier = dialogueAnswer.grantedAdvancement();
			Identifier criterionName = dialogueAnswer.criterionName();
			if (advancementIdentifier != null && criterionName != null) {
                AdvancementEntry advancementEntry = server.getAdvancementLoader().get(advancementIdentifier);
				if (advancementEntry != null) {
					serverPlayerEntity.getAdvancementTracker().grantCriterion(advancementEntry, criterionName.toString());
				}
			}

			// overlay message
			String overlayMessage = dialogueAnswer.overlayMessage();
			if (overlayMessage != null) {
				serverPlayerEntity.sendMessageToClient(Text.translatable(overlayMessage), true);
			}

			Identifier responseDialogueIdentifierString = dialogueAnswer.responseDialogue();
			if (responseDialogueIdentifierString == null) {
				serverPlayerEntity.closeHandledScreen();
			} else {
				ServerPlayNetworking.send(serverPlayerEntity, new OpenDialogueScreenPacket(responseDialogueIdentifierString.toString(), dialogueUsedBlocks, dialogueTriggeredBlocks));
			}


			// trigger block
			String triggeredBlock = dialogueAnswer.triggeredBlock();
			if (triggeredBlock != null) {
				for (MutablePair<String, MutablePair<BlockPos, Boolean>> entry : dialogueTriggeredBlocks) {
					if (entry.getLeft().equals(triggeredBlock)) {
						BlockEntity blockEntity = serverPlayerEntity.getWorld().getBlockEntity(entry.getRight().getLeft());
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
			String usedBlock = dialogueAnswer.usedBlock();
			if (usedBlock != null) {
				for (MutablePair<String, BlockPos> entry : dialogueUsedBlocks) {
					if (entry.getLeft().equals(usedBlock)) {
						BlockHitResult blockHitResult = new BlockHitResult(serverPlayerEntity.getPos(), Direction.UP, entry.getRight(), false);
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
