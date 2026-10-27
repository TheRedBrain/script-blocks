package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CopyDataBlockEntity extends RotatedBlockEntity implements Triggerable {
	private BlockPos firstDataProvidingBlockPosOffset = BlockPos.ZERO;
	private BlockPos secondDataProvidingBlockPosOffset = BlockPos.ZERO;
	private String dataIdentifier = "";

	public CopyDataBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.COPY_DATA_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("first_data_providing_block_pos_offset", BlockPos.CODEC, this.firstDataProvidingBlockPosOffset);

		view.store("second_data_providing_block_pos_offset", BlockPos.CODEC, this.secondDataProvidingBlockPosOffset);

		view.putString("data_identifier", this.dataIdentifier);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.firstDataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("first_data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.secondDataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("second_data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.dataIdentifier = view.getStringOr("data_identifier", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---
	public BlockPos getFirstDataProvidingBlockPosOffset() {
		return this.firstDataProvidingBlockPosOffset;
	}

	public void setFirstDataProvidingBlockPosOffset(BlockPos firstDataProvidingBlockPosOffset) {
		this.firstDataProvidingBlockPosOffset = firstDataProvidingBlockPosOffset;
	}

	public BlockPos getSecondDataProvidingBlockPosOffset() {
		return this.secondDataProvidingBlockPosOffset;
	}

	public void setSecondDataProvidingBlockPosOffset(BlockPos secondDataProvidingBlockPosOffset) {
		this.secondDataProvidingBlockPosOffset = secondDataProvidingBlockPosOffset;
	}

	public String getDataIdentifier() {
		return this.dataIdentifier;
	}

	public void setDataIdentifier(String dataIdentifier) {
		this.dataIdentifier = dataIdentifier;
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.level != null) {
			if (this.firstDataProvidingBlockPosOffset != BlockPos.ZERO && this.secondDataProvidingBlockPosOffset != BlockPos.ZERO) {
				BlockPos firstDataProviderBlockPos = new BlockPos(this.worldPosition.getX() + this.firstDataProvidingBlockPosOffset.getX(), this.worldPosition.getY() + this.firstDataProvidingBlockPosOffset.getY(), this.worldPosition.getZ() + this.firstDataProvidingBlockPosOffset.getZ());
				BlockPos secondDataProviderBlockPos = new BlockPos(this.worldPosition.getX() + this.secondDataProvidingBlockPosOffset.getX(), this.worldPosition.getY() + this.secondDataProvidingBlockPosOffset.getY(), this.worldPosition.getZ() + this.secondDataProvidingBlockPosOffset.getZ());

				BlockEntity firstBlockEntity = level.getBlockEntity(firstDataProviderBlockPos);
				BlockEntity secondBlockEntity = level.getBlockEntity(secondDataProviderBlockPos);
				if (firstBlockEntity instanceof ProvidesData firstProvidesDataEntity && secondBlockEntity instanceof ProvidesData secondProvidesDataEntity) {
					secondProvidesDataEntity.setData(this.dataIdentifier, firstProvidesDataEntity.getData(this.dataIdentifier));
				}
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.firstDataProvidingBlockPosOffset, blockRotation);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.secondDataProvidingBlockPosOffset, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, Mirror.FRONT_BACK);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, Mirror.LEFT_RIGHT);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
