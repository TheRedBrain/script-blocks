package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DataRelayBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable, ProvidesData {
	private final List<BlockPos> dataProvidingBlockPosOffsetList = new ArrayList<>();
	private int index = 0;

	public DataRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.DATA_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound;
		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("data_providing_blocks", CompoundTag.CODEC);
		for (BlockPos blockPos : this.dataProvidingBlockPosOffsetList) {
			nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, blockPos);
			listAppender.add(nbtCompound.copy());
		}

		view.putInt("index", this.index);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.dataProvidingBlockPosOffsetList.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("data_providing_blocks", CompoundTag.CODEC)) {
			this.dataProvidingBlockPosOffsetList.add(
					BlockPos.max(
							BlockPos.min(
									nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					)
			);
		}

		this.index = view.getIntOr("index", 0);

		super.loadAdditional(view);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	@Override
	public String getData(String id) {
		BlockPos dataProvidingBlockPos = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPos != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity1 = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPos));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				return providesDataBlockEntity.getData(id);
			}
		}
		return "";
	}

	@Override
	public void setData(String id, String value) {
		BlockPos dataProvidingBlockPosOffset = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity1 = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
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
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
			return this.dataProvidingBlockPosOffsetList.getLast();
		} else {
			this.index = 0;
			this.setChanged();
			if (this.level != null) {
				BlockState blockState = this.level.getBlockState(this.worldPosition);
				this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
			}
			return BlockPos.ZERO;
		}
	}

	private BlockPos getActualDataProvidingBlockPos(BlockPos dataProvidingBlockPosOffset) {
		return this.getBlockPos().offset(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ());
	}

	// region --- getter & setter ---
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
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		this.index++;
		if (this.index >= this.dataProvidingBlockPosOffsetList.size()) {
			this.index = 0;
		}
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void reset() {
		BlockPos dataProvidingBlockPosOffset = this.getCurrentDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock, blockRotation));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock, Mirror.FRONT_BACK));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<BlockPos> newList = new ArrayList<>();
				for (BlockPos triggeredBlock : this.dataProvidingBlockPosOffsetList) {
					newList.add(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock, Mirror.LEFT_RIGHT));
				}
				this.dataProvidingBlockPosOffsetList.clear();
				this.dataProvidingBlockPosOffsetList.addAll(newList);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
