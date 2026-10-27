package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.MimicBlock;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MimicBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
	private BlockPos activeMimicBlockPositionOffset = new BlockPos(0, 1, 0);
	private BlockPos inactiveMimicBlockPositionOffset = new BlockPos(0, -1, 0);

	public MimicBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.MIMIC_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("active_mimic_block_position_offset", BlockPos.CODEC, this.activeMimicBlockPositionOffset);

		view.store("inactive_mimic_block_position_offset", BlockPos.CODEC, this.inactiveMimicBlockPositionOffset);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.activeMimicBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("active_mimic_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.inactiveMimicBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("inactive_mimic_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

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
	public BlockPos getActiveMimicBlockPositionOffset() {
		return activeMimicBlockPositionOffset;
	}

	public boolean setActiveMimicBlockPositionOffset(BlockPos activeMimicBlockPositionOffset) {
		if (activeMimicBlockPositionOffset.getX() == 0 && activeMimicBlockPositionOffset.getY() == 0 && activeMimicBlockPositionOffset.getZ() == 0) {
			return false;
		}
		this.activeMimicBlockPositionOffset = activeMimicBlockPositionOffset;
		return true;
	}

	public BlockPos getInactiveMimicBlockPositionOffset() {
		return inactiveMimicBlockPositionOffset;
	}

	public boolean setInactiveMimicBlockPositionOffset(BlockPos inactiveMimicBlockPositionOffset) {
		if (inactiveMimicBlockPositionOffset.getX() == 0 && inactiveMimicBlockPositionOffset.getY() == 0 && inactiveMimicBlockPositionOffset.getZ() == 0) {
			return false;
		}
		this.inactiveMimicBlockPositionOffset = inactiveMimicBlockPositionOffset;
		return true;
	}
	// endregion --- getter & setter ---

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.activeMimicBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.activeMimicBlockPositionOffset, blockRotation);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.inactiveMimicBlockPositionOffset, blockRotation);
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.activeMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.activeMimicBlockPositionOffset, Mirror.FRONT_BACK);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.inactiveMimicBlockPositionOffset, Mirror.FRONT_BACK);
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.activeMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.activeMimicBlockPositionOffset, Mirror.LEFT_RIGHT);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.inactiveMimicBlockPositionOffset, Mirror.LEFT_RIGHT);
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.level != null) {
			BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition);
			if (blockEntity != null && blockEntity.getBlockState().getBlock() instanceof MimicBlock) {
				this.level.setBlockAndUpdate(this.worldPosition, blockEntity.getBlockState().setValue(BlockStateProperties.TRIGGERED, false));
			}
		}
	}

	@Override
	public void trigger() {
		if (this.level != null) {
			BlockEntity blockEntity = this.level.getBlockEntity(this.worldPosition);
			if (blockEntity != null && blockEntity.getBlockState().getBlock() instanceof MimicBlock) {
				this.level.setBlockAndUpdate(this.worldPosition, blockEntity.getBlockState().setValue(BlockStateProperties.TRIGGERED, true));
			}
		}
	}

	public BlockState getCurrentMimicBlockState() {
		BlockState fallbackMimicBlockState = ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.defaultBlockState();
		if (this.level != null && this.level.getBlockState(this.worldPosition).is(ScriptBlocksBlocks.MIMIC_BLOCK)) {
			BlockPos mimicBlockPos;
			if (this.level.getBlockState(this.worldPosition).getValue(BlockStateProperties.TRIGGERED)) {
				mimicBlockPos = worldPosition.offset(this.activeMimicBlockPositionOffset.getX(), this.activeMimicBlockPositionOffset.getY(), this.activeMimicBlockPositionOffset.getZ());
			} else {
				mimicBlockPos = worldPosition.offset(this.inactiveMimicBlockPositionOffset.getX(), this.inactiveMimicBlockPositionOffset.getY(), this.inactiveMimicBlockPositionOffset.getZ());
			}
			BlockState mimicBlockState = level.getBlockState(mimicBlockPos);
			if (!mimicBlockState.is(ScriptBlocksBlocks.MIMIC_BLOCK)) {
				return mimicBlockState;
			}
		}
		return fallbackMimicBlockState;
	}
}
