package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.MimicBlock;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MimicBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
	private BlockPos activeMimicBlockPositionOffset = new BlockPos(0, 1, 0);
	private BlockPos inactiveMimicBlockPositionOffset = new BlockPos(0, -1, 0);

	public MimicBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.MIMIC_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.put("active_mimic_block_position_offset", NbtHelper.fromBlockPos(this.activeMimicBlockPositionOffset));

		nbt.put("inactive_mimic_block_position_offset", NbtHelper.fromBlockPos(this.inactiveMimicBlockPositionOffset));

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("activeMimicBlockPositionOffsetX") || nbt.contains("activeMimicBlockPositionOffsetY") || nbt.contains("activeMimicBlockPositionOffsetZ")) {
			this.activeMimicBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("activeMimicBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("activeMimicBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("activeMimicBlockPositionOffsetZ"), -48, 48)
			);
			nbt.remove("activeMimicBlockPositionOffsetX");
			nbt.remove("activeMimicBlockPositionOffsetY");
			nbt.remove("activeMimicBlockPositionOffsetZ");
		} else if (nbt.contains("active_mimic_block_position_offset_x") || nbt.contains("active_mimic_block_position_offset_y") || nbt.contains("active_mimic_block_position_offset_z")) {
			this.activeMimicBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("active_mimic_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("active_mimic_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("active_mimic_block_position_offset_z"), -48, 48)
			);
			nbt.remove("active_mimic_block_position_offset_x");
			nbt.remove("active_mimic_block_position_offset_y");
			nbt.remove("active_mimic_block_position_offset_z");
		} else {
			this.activeMimicBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "active_mimic_block_position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("inactiveMimicBlockPositionOffsetX") || nbt.contains("inactiveMimicBlockPositionOffsetY") || nbt.contains("inactiveMimicBlockPositionOffsetZ")) {
			this.inactiveMimicBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("inactiveMimicBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("inactiveMimicBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("inactiveMimicBlockPositionOffsetZ"), -48, 48)
			);
			nbt.remove("inactiveMimicBlockPositionOffsetX");
			nbt.remove("inactiveMimicBlockPositionOffsetY");
			nbt.remove("inactiveMimicBlockPositionOffsetZ");
		} else if (nbt.contains("inactive_mimic_block_position_offset_x") || nbt.contains("inactive_mimic_block_position_offset_y") || nbt.contains("inactive_mimic_block_position_offset_z")) {
			this.inactiveMimicBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("inactive_mimic_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("inactive_mimic_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("inactive_mimic_block_position_offset_z"), -48, 48)
			);
			nbt.remove("inactive_mimic_block_position_offset_x");
			nbt.remove("inactive_mimic_block_position_offset_y");
			nbt.remove("inactive_mimic_block_position_offset_z");
		} else {
			this.inactiveMimicBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "inactive_mimic_block_position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.activeMimicBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.activeMimicBlockPositionOffset, blockRotation);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.inactiveMimicBlockPositionOffset, blockRotation);
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.activeMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.activeMimicBlockPositionOffset, BlockMirror.FRONT_BACK);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.inactiveMimicBlockPositionOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.activeMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.activeMimicBlockPositionOffset, BlockMirror.LEFT_RIGHT);
				this.inactiveMimicBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.inactiveMimicBlockPositionOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	@Override
	public void reset() {
		if (this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.pos);
			if (blockEntity != null && blockEntity.getCachedState().getBlock() instanceof MimicBlock) {
				this.world.setBlockState(this.pos, blockEntity.getCachedState().with(Properties.TRIGGERED, false));
			}
		}
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.pos);
			if (blockEntity != null && blockEntity.getCachedState().getBlock() instanceof MimicBlock) {
				this.world.setBlockState(this.pos, blockEntity.getCachedState().with(Properties.TRIGGERED, true));
			}
		}
	}

	public BlockState getCurrentMimicBlockState() {
		BlockState fallbackMimicBlockState = ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.getDefaultState();
		if (this.world != null && this.world.getBlockState(this.pos).isOf(ScriptBlocksBlocks.MIMIC_BLOCK)) {
			BlockPos mimicBlockPos;
			if (this.world.getBlockState(this.pos).get(Properties.TRIGGERED)) {
				mimicBlockPos = pos.add(this.activeMimicBlockPositionOffset.getX(), this.activeMimicBlockPositionOffset.getY(), this.activeMimicBlockPositionOffset.getZ());
			} else {
				mimicBlockPos = pos.add(this.inactiveMimicBlockPositionOffset.getX(), this.inactiveMimicBlockPositionOffset.getY(), this.inactiveMimicBlockPositionOffset.getZ());
			}
			BlockState mimicBlockState = world.getBlockState(mimicBlockPos);
			if (!mimicBlockState.isOf(ScriptBlocksBlocks.MIMIC_BLOCK)) {
				return mimicBlockState;
			}
		}
		return fallbackMimicBlockState;
	}
}
