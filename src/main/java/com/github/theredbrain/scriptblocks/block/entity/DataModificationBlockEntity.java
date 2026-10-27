package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DataModificationBlockEntity extends RotatedBlockEntity implements ProvidesData {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
	private DataModificationMode dataModificationMode = DataModificationMode.INTEGER_ADDITION;
	private int addedIntegerValue = 0;
	private int integerModulusValue = 0;

	public DataModificationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.DATA_MODIFICATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("data_providing_block_pos_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("data_modification_mode", this.dataModificationMode.getSerializedName());

		view.putInt("added_integer_value", this.addedIntegerValue);

		view.putInt("integer_modulus_value", this.integerModulusValue);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.dataModificationMode = DataModificationMode.byName(view.getStringOr("data_modification_mode", "")).orElseGet(() -> DataModificationMode.INTEGER_ADDITION);

		this.addedIntegerValue = view.getIntOr("added_integer_value", 0);

		this.integerModulusValue = view.getIntOr("integer_modulus_value", 0);

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

	public int getIntegerModulusValue() {
		return this.integerModulusValue;
	}

	public void setIntegerModulusValue(int integerModulusValue) {
		this.integerModulusValue = integerModulusValue;
	}
	// endregion --- getter & setter ---

	@Override
	public String getData(String id) {
		BlockPos dataProvidingBlockPos = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPos != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity1 = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPos));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				return modifyData(providesDataBlockEntity.getData(id));
			}
		}
		return "";
	}

	@Override
	public void setData(String id, String value) {
		BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity1 = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity1 instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.setData(id, modifyData(value));
			}
		}
	}

	@Override
	public void reset() {
		BlockPos dataProvidingBlockPosOffset = this.getDataProvidingBlockPosOffset();
		if (dataProvidingBlockPosOffset != BlockPos.ZERO && this.level != null) {
			BlockEntity blockEntity = this.level.getBlockEntity(this.getActualDataProvidingBlockPos(dataProvidingBlockPosOffset));
			if (blockEntity instanceof ProvidesData providesDataBlockEntity) {
				providesDataBlockEntity.reset();
			}
		}
	}

	private String modifyData(String originalData) {
		if (this.dataModificationMode == DataModificationMode.INTEGER_ADDITION) {
			return Integer.toString(ItemUtils.parseInt(originalData) + this.addedIntegerValue);
		} else if (this.dataModificationMode == DataModificationMode.INTEGER_MODULO) {
			return Integer.toString(ItemUtils.parseInt(originalData) % this.integerModulusValue);
		} else {
			return originalData;
		}
	}

	private BlockPos getActualDataProvidingBlockPos(BlockPos dataProvidingBlockPosOffset) {
		return this.getBlockPos().offset(dataProvidingBlockPosOffset.getX(), dataProvidingBlockPosOffset.getY(), dataProvidingBlockPosOffset.getZ());
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public enum DataModificationMode implements StringRepresentable {
		INTEGER_ADDITION("integer_addition"),
		INTEGER_MODULO("integer_modulo");

		private final String name;

		private DataModificationMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<DataModificationMode> byName(String name) {
			return Arrays.stream(DataModificationMode.values()).filter(dataModificationMode -> dataModificationMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.data_modification_block.data_modification_mode." + this.name);
		}
	}
}
