package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.block.TriggeredRedstoneBlock;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TriggeredRedstoneBlockEntity extends BlockEntity implements Triggerable {

	public TriggeredRedstoneBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_REDSTONE_BLOCK_ENTITY, pos, state);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	@Override
	public void trigger() {
		if (this.level != null) {
			BlockState blockState = level.getBlockState(this.worldPosition);
			if (blockState.getBlock() instanceof TriggeredRedstoneBlock triggeredRedstoneBlock) {
				triggeredRedstoneBlock.trigger(this.level, blockState, this.worldPosition);
			}
		}
	}
}
