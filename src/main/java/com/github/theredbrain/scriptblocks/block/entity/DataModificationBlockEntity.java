package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;
import java.util.Optional;

public class DataModificationBlockEntity extends RotatedBlockEntity implements ProvidesData {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private DataModificationMode dataModificationMode = DataModificationMode.INTEGER_ADDITION;
	private int addedIntegerValue = 0;

	public DataModificationBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_MODIFICATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.dataProvidingBlockPosOffset != DATA_PROVIDING_BLOCK_POS_DEFAULT) {
			nbt.putInt("data_providing_block_pos_offset_x", this.dataProvidingBlockPosOffset.getX());
			nbt.putInt("data_providing_block_pos_offset_y", this.dataProvidingBlockPosOffset.getY());
			nbt.putInt("data_providing_block_pos_offset_z", this.dataProvidingBlockPosOffset.getZ());
		}

		nbt.putString("data_modification_mode", this.dataModificationMode.asString());

		nbt.putInt("added_integer_value", this.addedIntegerValue);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("data_providing_block_pos_offset_x", NbtElement.INT_TYPE) && nbt.contains("data_providing_block_pos_offset_y", NbtElement.INT_TYPE) && nbt.contains("data_providing_block_pos_offset_z", NbtElement.INT_TYPE)) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("data_providing_block_pos_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("data_providing_block_pos_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("data_providing_block_pos_offset_z"), -48, 48)
			);
		} else {
			this.dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
		}

		this.dataModificationMode = DataModificationMode.byName(nbt.getString("data_modification_mode")).orElseGet(() -> DataModificationMode.INTEGER_ADDITION);

		this.addedIntegerValue = nbt.getInt("added_integer_value");

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

	public DataModificationMode getDataModificationMode() {
		return this.dataModificationMode;
	}

	public void setDataModificationMode(DataModificationMode dataModificationMode) {
		this.dataModificationMode = dataModificationMode;
	}

	public int getAddedIntegerValue() {
		return this.addedIntegerValue;
	}

	public void setAddedIntegerValue(int addedIntegerValue) {
		this.addedIntegerValue = addedIntegerValue;
	}
	// endregion --- getter & setter ---

	@Override
	public String getData(String id) {
		BlockPos dataProvidingBlockPos = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPos != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPos));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				return modifyData(providesDataBlockEntity.getData(id));
			}
		}
		return "";
	}

	@Override
	public void setData(String id, String value) {
		BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity1 = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.setData(id, modifyData(value));
			}
		}
	}

	@Override
	public void reset() {
		BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ORIGIN && this.world != null) {
			BlockEntity blockEntity = this.world.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.reset();
			}
		}
	}

	private String modifyData(String originalData) {
		if (this.dataModificationMode == DataModificationMode.INTEGER_ADDITION) {
			return Integer.toString(ItemUtils.parseInt(originalData) + this.addedIntegerValue);
		} else {
			return originalData;
		}
	}

	private BlockPos getActualDataProvidingBlockPos(BlockPos dataProvidingBlockPosOffset) {
		return this.getPos().add(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ());
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

	public enum DataModificationMode implements StringIdentifiable {
		INTEGER_ADDITION("integer_addition");

		private final String name;

		private DataModificationMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<DataModificationMode> byName(String name) {
			return Arrays.stream(DataModificationMode.values()).filter(dataModificationMode -> dataModificationMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.data_modification_block.data_modification_mode." + this.name);
		}
	}
}
