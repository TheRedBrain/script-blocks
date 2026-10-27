package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.screen.TriggeredDispenserBlockScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriggeredDispenserBlockEntity extends DispenserBlockEntity {
	public TriggeredDispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ScriptBlocksEntities.TRIGGERED_DISPENSER_BLOCK_ENTITY, blockPos, blockState);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.triggered_dispenser");
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
		return new TriggeredDispenserBlockScreenHandler(syncId, playerInventory, this);
	}
}
