package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.screen.TriggeredDispenserBlockScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class TriggeredDispenserBlockEntity extends DispenserBlockEntity {
	public TriggeredDispenserBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(EntityRegistry.TRIGGERED_DISPENSER_BLOCK_ENTITY, blockPos, blockState);
	}

	@Override
	protected Text getContainerName() {
		return Text.translatable("container.triggered_dispenser");
	}

	@Override
	protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
		return new TriggeredDispenserBlockScreenHandler(syncId, playerInventory, this);
	}
}
