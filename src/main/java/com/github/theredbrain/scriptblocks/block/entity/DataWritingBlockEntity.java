package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class DataWritingBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private String dataIdentifier = "";
	private String newDataValue = "";

	public DataWritingBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_WRITING_BLOCK_ENTITY, pos, state);
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

		if (!this.dataIdentifier.isEmpty()) {
			nbt.putString("dataIdentifier", this.dataIdentifier);
		} else {
			nbt.remove("dataIdentifier");
		}

		if (!this.newDataValue.isEmpty()) {
			nbt.putString("newDataValue", this.newDataValue);
		} else {
			nbt.remove("newDataValue");
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
		} else {
			this.dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
		}

		if (nbt.contains("dataIdentifier", NbtElement.STRING_TYPE)) {
			this.dataIdentifier = nbt.getString("dataIdentifier");
		} else {
			this.dataIdentifier = "";
		}

		if (nbt.contains("newDataValue", NbtElement.STRING_TYPE)) {
			this.newDataValue = nbt.getString("newDataValue");
		} else {
			this.newDataValue = "";
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
	public BlockPos getDataProvidingBlockPosOffset() {
		return dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public String getDataIdentifier() {
		return this.dataIdentifier;
	}

	public void setDataIdentifier(String dataIdentifier) {
		this.dataIdentifier = dataIdentifier;
	}

	public String getNewDataValue() {
		return this.newDataValue;
	}

	public void setNewDataValue(String newDataValue) {
		this.newDataValue = newDataValue;
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.world != null) {
			BlockPos dataProviderBlockPos = new BlockPos(this.pos.getX() + this.dataProvidingBlockPosOffset.getX(), this.pos.getY() + this.dataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.dataProvidingBlockPosOffset.getZ());

			BlockEntity blockEntity = world.getBlockEntity(dataProviderBlockPos);
			if (blockEntity instanceof ProvidesData providesDataEntity) {
				providesDataEntity.setData(this.dataIdentifier, this.newDataValue);
			}
		}
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
