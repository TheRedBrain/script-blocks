package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public class CopyDataBlockEntity extends RotatedBlockEntity implements Triggerable {
	private BlockPos firstDataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private BlockPos secondDataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private String dataIdentifier = "";

	public CopyDataBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.COPY_DATA_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		view.put("first_data_providing_block_pos_offset", BlockPos.CODEC, this.firstDataProvidingBlockPosOffset);

		view.put("second_data_providing_block_pos_offset", BlockPos.CODEC, this.secondDataProvidingBlockPosOffset);

		view.putString("data_identifier", this.dataIdentifier);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.firstDataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("first_data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.secondDataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("second_data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.dataIdentifier = view.getString("data_identifier", "");

		super.readData(view);
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.firstDataProvidingBlockPosOffset, blockRotation);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.secondDataProvidingBlockPosOffset, blockRotation);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.firstDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.firstDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.secondDataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.secondDataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
