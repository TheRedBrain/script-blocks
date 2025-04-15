package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.block.TriggeredRedstoneBlock;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class TriggeredRedstoneBlockEntity extends BlockEntity implements Triggerable {

	public TriggeredRedstoneBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERED_REDSTONE_BLOCK_ENTITY, pos, state);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			BlockState blockState = world.getBlockState(this.pos);
			if (blockState.getBlock() instanceof TriggeredRedstoneBlock triggeredRedstoneBlock) {
				triggeredRedstoneBlock.trigger(this.world, blockState, this.pos);
			}
		}
	}
}
