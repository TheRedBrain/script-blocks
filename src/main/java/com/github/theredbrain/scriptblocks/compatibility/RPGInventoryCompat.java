package com.github.theredbrain.scriptblocks.compatibility;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class RPGInventoryCompat {

	public static ItemStack getRPGEquipmentStack(PlayerEntity playerEntity, int index) {
		return switch (index) {
//			case 4 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.CLASS_ITEM);
//			case 5 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SHOULDERS);
//			case 6 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.GLOVES);
//			case 7 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.BELT);
//			case 8 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.NECKLACE);
//			case 9 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.RING_1);
//			case 10 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.RING_2);
//			case 11 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.RELIC);
//			case 12 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_1);
//			case 13 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_2);
//			case 14 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_3);
//			case 15 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_4);
//			case 16 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_5);
//			case 17 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_6);
//			case 18 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_7);
//			case 19 -> playerEntity.getEquippedStack(ExtendedEquipmentSlot.SPELL_8);
			default -> ItemStack.EMPTY;
		};
	}

	public static void setRPGEquipmentStack(PlayerEntity playerEntity, int index, ItemStack stack) {
		switch (index) {
//			case 4 -> playerEntity.equipStack(ExtendedEquipmentSlot.CLASS_ITEM, stack);
//			case 5 -> playerEntity.equipStack(ExtendedEquipmentSlot.SHOULDERS, stack);
//			case 6 -> playerEntity.equipStack(ExtendedEquipmentSlot.GLOVES, stack);
//			case 7 -> playerEntity.equipStack(ExtendedEquipmentSlot.BELT, stack);
//			case 8 -> playerEntity.equipStack(ExtendedEquipmentSlot.NECKLACE, stack);
//			case 9 -> playerEntity.equipStack(ExtendedEquipmentSlot.RING_1, stack);
//			case 10 -> playerEntity.equipStack(ExtendedEquipmentSlot.RING_2, stack);
//			case 11 -> playerEntity.equipStack(ExtendedEquipmentSlot.RELIC, stack);
//			case 12 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_1, stack);
//			case 13 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_2, stack);
//			case 14 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_3, stack);
//			case 15 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_4, stack);
//			case 16 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_5, stack);
//			case 17 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_6, stack);
//			case 18 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_7, stack);
//			case 19 -> playerEntity.equipStack(ExtendedEquipmentSlot.SPELL_8, stack);
		}

	}

}
