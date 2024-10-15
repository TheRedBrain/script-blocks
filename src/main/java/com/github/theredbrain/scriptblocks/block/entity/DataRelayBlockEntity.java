package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class DataRelayBlockEntity extends RotatedBlockEntity implements Resetable, ProvidesData {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;

	public DataRelayBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.dataProvidingBlockPosOffset != DATA_PROVIDING_BLOCK_POS_DEFAULT) {
			nbt.putInt("dataProvidingBlockPosOffsetX", this.dataProvidingBlockPosOffset.getX());
			nbt.putInt("dataProvidingBlockPosOffsetY", this.dataProvidingBlockPosOffset.getY());
			nbt.putInt("dataProvidingBlockPosOffsetZ", this.dataProvidingBlockPosOffset.getZ());
		} else {
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("dataProvidingBlockPosOffsetX", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetY", NbtElement.INT_TYPE) && nbt.contains("dataProvidingBlockPosOffsetZ", NbtElement.INT_TYPE)) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
		}

		super.readNbt(nbt, registryLookup);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	@Override
	public int getData(String id) {
		BlockPos dataProvidingBlockPos = this.dataProvidingBlockPosOffset;
		if (dataProvidingBlockPos != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos());
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				return providesDataBlockEntity.getData(id);
			}
		}
		return 0;
	}

	@Override
	public void setData(String id, int value) {
		BlockPos dataProvidingBlockPos = this.dataProvidingBlockPosOffset;
		if (dataProvidingBlockPos != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos());
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.setData(id, value);
			}
		}
	}

	@Override
	public void addData(String id, int value) {
		BlockPos dataProvidingBlockPos = this.dataProvidingBlockPosOffset;
		if (dataProvidingBlockPos != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos());
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.addData(id, value);
			}
		}
	}

	private BlockPos getActualDataProvidingBlockPos() {
		return this.getPos().add(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ());
	}

	@Override
	public void reset() {
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
