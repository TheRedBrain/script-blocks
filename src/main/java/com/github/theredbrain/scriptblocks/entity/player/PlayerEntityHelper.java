package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.RemovedOnTeleportComponent;
import com.github.theredbrain.scriptblocks.registry.ItemComponentRegistry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class PlayerEntityHelper {

	private static final int EXCLUSIVE_EQUIPMENT_SLOT_AMOUNT = 20;

	public static void removeItemsOnTeleport(ServerPlayerEntity serverPlayerEntity, String removedItemIdentifier) {
		if (removedItemIdentifier.isEmpty()) {
			return;
		}
		for (int i = 0; i < EXCLUSIVE_EQUIPMENT_SLOT_AMOUNT; i++) {
			ItemStack currentItemStack = getEquipmentStack(serverPlayerEntity, i).copy();
			if (currentItemStack.isEmpty()) {
				continue;
			}
			RemovedOnTeleportComponent removedOnTeleportDataComponent = currentItemStack.get(ItemComponentRegistry.REMOVED_ON_TELEPORT);
			if (removedOnTeleportDataComponent != null) {
				for (String identifier : removedOnTeleportDataComponent.identifier_list()) {
					if (Objects.equals(identifier, removedItemIdentifier)) {
						setEquipmentStack(serverPlayerEntity, i, ItemStack.EMPTY);
					}
				}
			}
		}

		PlayerInventory playerInventory = serverPlayerEntity.getInventory();
		for (int i = 0; i < playerInventory.main.size(); i++) {
			ItemStack currentItemStack = playerInventory.main.get(i);
			RemovedOnTeleportComponent removedOnTeleportDataComponent = currentItemStack.get(ItemComponentRegistry.REMOVED_ON_TELEPORT);
			if (removedOnTeleportDataComponent != null) {
				for (String identifier : removedOnTeleportDataComponent.identifier_list()) {
					if (Objects.equals(identifier, removedItemIdentifier)) {
						playerInventory.main.set(i, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	private static ItemStack getEquipmentStack(PlayerEntity playerEntity, int index) {
		return switch (index) {
			case 0 -> playerEntity.getEquippedStack(EquipmentSlot.HEAD);
			case 1 -> playerEntity.getEquippedStack(EquipmentSlot.CHEST);
			case 2 -> playerEntity.getEquippedStack(EquipmentSlot.LEGS);
			case 3 -> playerEntity.getEquippedStack(EquipmentSlot.FEET);
			default -> ScriptBlocks.getRPGEquipmentStack(playerEntity, index);
		};
	}

	private static void setEquipmentStack(PlayerEntity playerEntity, int index, ItemStack stack) {
		switch (index) {
			case 0 -> playerEntity.equipStack(EquipmentSlot.HEAD, stack);
			case 1 -> playerEntity.equipStack(EquipmentSlot.CHEST, stack);
			case 2 -> playerEntity.equipStack(EquipmentSlot.LEGS, stack);
			case 3 -> playerEntity.equipStack(EquipmentSlot.FEET, stack);
			default -> ScriptBlocks.setRPGEquipmentStack(playerEntity, index, stack);
		}

	}

}
