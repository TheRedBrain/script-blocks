package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.PreventInteractionsDataComponent;
import com.github.theredbrain.scriptblocks.component.type.RemovedOnInteractionsDataComponent;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataComponents;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerEntityHelper {

	private static final int EXCLUSIVE_EQUIPMENT_SLOT_AMOUNT = 20;

	// TODO rework into dedicated ItemRemoverBlock
	public static void removeItemsOnTeleport(ServerPlayer serverPlayerEntity, String removedItemIdentifier) {
		if (removedItemIdentifier.isEmpty()) {
			return;
		}
		for (int i = 0; i < EXCLUSIVE_EQUIPMENT_SLOT_AMOUNT; i++) {
			ItemStack currentItemStack = getEquipmentStack(serverPlayerEntity, i).copy();
			if (currentItemStack.isEmpty()) {
				continue;
			}
			RemovedOnInteractionsDataComponent removedOnTeleportDataComponent = currentItemStack.get(ScriptBlocksDataComponents.REMOVED_ON_INTERACTIONS);
			if (removedOnTeleportDataComponent != null) {
				for (String identifier : removedOnTeleportDataComponent.identifier_list()) {
					if (Objects.equals(identifier, removedItemIdentifier)) {
						setEquipmentStack(serverPlayerEntity, i, ItemStack.EMPTY);
					}
				}
			}
		}

		Inventory playerInventory = serverPlayerEntity.getInventory();
		for (int i = 0; i < playerInventory.items.size(); i++) {
			ItemStack currentItemStack = playerInventory.items.get(i).copy();
			RemovedOnInteractionsDataComponent removedOnTeleportDataComponent = currentItemStack.get(ScriptBlocksDataComponents.REMOVED_ON_INTERACTIONS);
			if (removedOnTeleportDataComponent != null) {
				for (String identifier : removedOnTeleportDataComponent.identifier_list()) {
					if (Objects.equals(identifier, removedItemIdentifier)) {
						playerInventory.items.set(i, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	public static boolean allowInteractions(Player playerEntity, String preventingItemIdentifier) {
		if (preventingItemIdentifier.isEmpty()) {
			return true;
		}
		for (int i = 0; i < EXCLUSIVE_EQUIPMENT_SLOT_AMOUNT; i++) {
			ItemStack currentItemStack = getEquipmentStack(playerEntity, i);
			if (currentItemStack.isEmpty()) {
				continue;
			}
			PreventInteractionsDataComponent preventInteractionsDataComponent = currentItemStack.get(ScriptBlocksDataComponents.PREVENT_INTERACTIONS);
			if (preventInteractionsDataComponent != null) {
				for (String identifier : preventInteractionsDataComponent.identifier_list()) {
					if (Objects.equals(identifier, preventingItemIdentifier)) {
						return false;
					}
				}
			}
		}

		Inventory playerInventory = playerEntity.getInventory();
		for (int i = 0; i < playerInventory.items.size(); i++) {
			ItemStack currentItemStack = playerInventory.items.get(i).copy();
			PreventInteractionsDataComponent preventInteractionsDataComponent = currentItemStack.get(ScriptBlocksDataComponents.PREVENT_INTERACTIONS);
			if (preventInteractionsDataComponent != null) {
				for (String identifier : preventInteractionsDataComponent.identifier_list()) {
					if (Objects.equals(identifier, preventingItemIdentifier)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static ItemStack getEquipmentStack(Player playerEntity, int index) {
		return switch (index) {
			case 0 -> playerEntity.getItemBySlot(EquipmentSlot.HEAD);
			case 1 -> playerEntity.getItemBySlot(EquipmentSlot.CHEST);
			case 2 -> playerEntity.getItemBySlot(EquipmentSlot.LEGS);
			case 3 -> playerEntity.getItemBySlot(EquipmentSlot.FEET);
			default -> ScriptBlocks.getRPGEquipmentStack(playerEntity, index);
		};
	}

	private static void setEquipmentStack(Player playerEntity, int index, ItemStack stack) {
		switch (index) {
			case 0 -> playerEntity.setItemSlot(EquipmentSlot.HEAD, stack);
			case 1 -> playerEntity.setItemSlot(EquipmentSlot.CHEST, stack);
			case 2 -> playerEntity.setItemSlot(EquipmentSlot.LEGS, stack);
			case 3 -> playerEntity.setItemSlot(EquipmentSlot.FEET, stack);
			default -> ScriptBlocks.setRPGEquipmentStack(playerEntity, index, stack);
		}

	}

}
