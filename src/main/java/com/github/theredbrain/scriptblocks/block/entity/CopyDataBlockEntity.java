package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
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

public class CopyDataBlockEntity extends RotatedBlockEntity implements Triggerable {
	private BlockPos firstDataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private BlockPos secondDataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private String dataIdentifier = "";

	public CopyDataBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.COPY_DATA_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("first_data_providing_block_pos_offset_x", this.firstDataProvidingBlockPosOffset.getX());
		nbt.putInt("first_data_providing_block_pos_offset_y", this.firstDataProvidingBlockPosOffset.getY());
		nbt.putInt("first_data_providing_block_pos_offset_z", this.firstDataProvidingBlockPosOffset.getZ());

		nbt.putInt("second_data_providing_block_pos_offset_x", this.secondDataProvidingBlockPosOffset.getX());
		nbt.putInt("second_data_providing_block_pos_offset_y", this.secondDataProvidingBlockPosOffset.getY());
		nbt.putInt("second_data_providing_block_pos_offset_z", this.secondDataProvidingBlockPosOffset.getZ());

		nbt.putString("data_identifier", this.dataIdentifier);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("firstDataProvidingBlockPosOffsetX") && nbt.contains("firstDataProvidingBlockPosOffsetY") && nbt.contains("firstDataProvidingBlockPosOffsetZ")) {
			this.firstDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstDataProvidingBlockPosOffsetZ"), -48, 48)
			);
			nbt.remove("firstDataProvidingBlockPosOffsetX");
			nbt.remove("firstDataProvidingBlockPosOffsetY");
			nbt.remove("firstDataProvidingBlockPosOffsetZ");
		} else {
			this.firstDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("first_data_providing_block_pos_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("first_data_providing_block_pos_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("first_data_providing_block_pos_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("secondDataProvidingBlockPosOffsetX") && nbt.contains("secondDataProvidingBlockPosOffsetY") && nbt.contains("secondDataProvidingBlockPosOffsetZ")) {
			this.secondDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondDataProvidingBlockPosOffsetZ"), -48, 48)
			);
			nbt.remove("secondDataProvidingBlockPosOffsetX");
			nbt.remove("secondDataProvidingBlockPosOffsetY");
			nbt.remove("secondDataProvidingBlockPosOffsetZ");
		} else {
			this.secondDataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("second_data_providing_block_pos_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("second_data_providing_block_pos_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("second_data_providing_block_pos_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("dataIdentifier")) {
			this.dataIdentifier = nbt.getString("dataIdentifier");
			nbt.remove("dataIdentifier");
		} else {
			this.dataIdentifier = nbt.getString("data_identifier");
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
		if (this.world != null) {
			if (this.firstDataProvidingBlockPosOffset != BlockPos.ORIGIN && this.secondDataProvidingBlockPosOffset != BlockPos.ORIGIN) {
				BlockPos firstDataProviderBlockPos = new BlockPos(this.pos.getX() + this.firstDataProvidingBlockPosOffset.getX(), this.pos.getY() + this.firstDataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.firstDataProvidingBlockPosOffset.getZ());
				BlockPos secondDataProviderBlockPos = new BlockPos(this.pos.getX() + this.secondDataProvidingBlockPosOffset.getX(), this.pos.getY() + this.secondDataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.secondDataProvidingBlockPosOffset.getZ());

				BlockEntity firstBlockEntity = world.getBlockEntity(firstDataProviderBlockPos);
				BlockEntity secondBlockEntity = world.getBlockEntity(secondDataProviderBlockPos);
				if (firstBlockEntity instanceof ProvidesData firstProvidesDataEntity && secondBlockEntity instanceof ProvidesData secondProvidesDataEntity) {
					secondProvidesDataEntity.setData(this.dataIdentifier, firstProvidesDataEntity.getData(this.dataIdentifier));
				}
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.firstDataProvidingBlockPosOffset, blockRotation);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.secondDataProvidingBlockPosOffset, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
