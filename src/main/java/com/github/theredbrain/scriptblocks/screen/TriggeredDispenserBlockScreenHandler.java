package com.github.theredbrain.scriptblocks.screen;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TriggeredDispenserBlockScreenHandler extends AbstractContainerMenu {
	private static final int CONTAINER_SIZE = 9;
	private static final int INVENTORY_START = 9;
	private static final int INVENTORY_END = 36;
	private static final int HOTBAR_START = 36;
	private static final int HOTBAR_END = 45;
	private final Container inventory;

	public TriggeredDispenserBlockScreenHandler(int syncId, Inventory playerInventory) {
		this(syncId, playerInventory, new SimpleContainer(9));
	}

	public TriggeredDispenserBlockScreenHandler(int syncId, Inventory playerInventory, Container inventory) {
		super(MenuType.GENERIC_3x3, syncId);
		checkContainerSize(inventory, 9);
		this.inventory = inventory;
		inventory.startOpen(playerInventory.player);

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				this.addSlot(new Slot(inventory, j + i * 3, 62 + j * 18, 17 + i * 18) {

					@Override
					public int getMaxStackSize() {
						return 1;
					}

				});
			}
		}

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++) {
			this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return this.inventory.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		ItemStack itemStack = ItemStack.EMPTY;
		Slot slot2 = this.slots.get(slot);
		if (slot2 != null && slot2.hasItem()) {
			ItemStack itemStack2 = slot2.getItem();
			itemStack = itemStack2.copy();
			if (slot < 9) {
				if (!this.moveItemStackTo(itemStack2, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack2, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack2.isEmpty()) {
				slot2.setByPlayer(ItemStack.EMPTY);
			} else {
				slot2.setChanged();
			}

			if (itemStack2.getCount() == itemStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot2.onTake(player, itemStack2);
		}

		return itemStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.inventory.stopOpen(player);
	}
}
