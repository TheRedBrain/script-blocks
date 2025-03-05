package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class DataRelayBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable, ProvidesData {
	private final List<BlockPos> dataProvidingBlockPosOffsetList = new ArrayList<>();
	private int index = 0;

	public DataRelayBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("dataProvidingBlockPosOffsetListSize", this.dataProvidingBlockPosOffsetList.size());
		for (int i = 0; i < this.dataProvidingBlockPosOffsetList.size(); i++) {
			BlockPos triggeredBlock = this.dataProvidingBlockPosOffsetList.get(i);
			nbt.putInt("dataProvidingBlockPosOffsetX_" + i, triggeredBlock.getX());
			nbt.putInt("dataProvidingBlockPosOffsetY_" + i, triggeredBlock.getY());
			nbt.putInt("dataProvidingBlockPosOffsetZ_" + i, triggeredBlock.getZ());
		}

		nbt.putInt("index", this.index);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int dataProvidingBlockPosOffsetListSize = nbt.getInt("dataProvidingBlockPosOffsetListSize");
		this.dataProvidingBlockPosOffsetList.clear();
		for (int i = 0; i < dataProvidingBlockPosOffsetListSize; i++) {
			this.dataProvidingBlockPosOffsetList.add(new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ_" + i), -48, 48)
			));
		}

		if (nbt.contains("index")) {
			this.index = nbt.getInt("index");
		} else {
			this.index = 0;
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
	public String getData(String id) {
		BlockPos dataProvidingBlockPos = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPos != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPos));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				return providesDataBlockEntity.getData(id);
			}
		}
		return "";
	}

	@Override
	public void setData(String id, String value) {
		BlockPos dataProvidingBlockPosOffset = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.setData(id, value);
			}
		}
	}

	private BlockPos getCurrentDataProvidingBlockPosOffset() {
		if (this.index > 0 && this.index < this.dataProvidingBlockPosOffsetList.size()) {
			return this.dataProvidingBlockPosOffsetList.get(this.index);
		} else if (!this.dataProvidingBlockPosOffsetList.isEmpty()) {
			this.index = this.dataProvidingBlockPosOffsetList.size() - 1;
			return this.dataProvidingBlockPosOffsetList.getLast();
		} else {
			this.index = 0;
			return BlockPos.ORIGIN;
		}
	}

	private BlockPos getActualDataProvidingBlockPos(BlockPos dataProvidingBlockPosOffset) {
		return this.getPos().add(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ());
	}

	public List<BlockPos> getDataProvidingBlockPosOffsetList() {
		return this.dataProvidingBlockPosOffsetList;
	}

	public void setDataProvidingBlockPosOffsetList(List<BlockPos> dataProvidingBlockPosOffsetList) {
		this.dataProvidingBlockPosOffsetList.clear();
		this.dataProvidingBlockPosOffsetList.addAll(dataProvidingBlockPosOffsetList);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public void trigger() {
		this.index++;
		if (this.index >= this.dataProvidingBlockPosOffsetList.size()) {
			this.index = 0;
		}
	}

	@Override
	public void reset() {
		BlockPos dataProvidingBlockPosOffset = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock, blockRotation));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock, BlockMirror.FRONT_BACK));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock, BlockMirror.LEFT_RIGHT));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
