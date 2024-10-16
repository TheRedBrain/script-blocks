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
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.Arrays;
import java.util.Optional;

public class DataAccessBlockEntity extends RotatedBlockEntity implements Triggerable {
	private static final BlockPos DATA_PROVIDING_BLOCK_POS_DEFAULT = new BlockPos(0, -1, 0);
	private static final BlockPos FIRST_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 0, 1);
	private static final BlockPos SECOND_TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(1, 0, 0);
	private BlockPos dataProvidingBlockPosOffset = DATA_PROVIDING_BLOCK_POS_DEFAULT;
	private MutablePair<BlockPos, Boolean> firstTriggeredBlock = new MutablePair<>(FIRST_TRIGGERED_BLOCK_POS_DEFAULT, false);
	private MutablePair<BlockPos, Boolean> secondTriggeredBlock = new MutablePair<>(SECOND_TRIGGERED_BLOCK_POS_DEFAULT, false);
	private boolean isWriting = false;
	private String dataIdentifier = "";
	// reading mode
	private int comparedDataValue = 0;
	private DataReadingMode dataReadingMode = DataReadingMode.LESSER;
	// writing mode
	private boolean isAdding = false;
	private int newDataValue = 0;

	public DataAccessBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_ACCESS_BLOCK_ENTITY, pos, state);
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

		if (this.firstTriggeredBlock.getLeft() != FIRST_TRIGGERED_BLOCK_POS_DEFAULT || this.firstTriggeredBlock.getRight()) {
			nbt.putInt("firstTriggeredBlockPositionOffsetX", this.firstTriggeredBlock.getLeft().getX());
			nbt.putInt("firstTriggeredBlockPositionOffsetY", this.firstTriggeredBlock.getLeft().getY());
			nbt.putInt("firstTriggeredBlockPositionOffsetZ", this.firstTriggeredBlock.getLeft().getZ());
			nbt.putBoolean("firstTriggeredBlockResets", this.firstTriggeredBlock.getRight());
		} else {
			nbt.remove("firstTriggeredBlockPositionOffsetX");
			nbt.remove("firstTriggeredBlockPositionOffsetY");
			nbt.remove("firstTriggeredBlockPositionOffsetZ");
			nbt.remove("firstTriggeredBlockResets");
		}

		if (this.secondTriggeredBlock.getLeft() != SECOND_TRIGGERED_BLOCK_POS_DEFAULT || this.secondTriggeredBlock.getRight()) {
			nbt.putInt("secondTriggeredBlockPositionOffsetX", this.secondTriggeredBlock.getLeft().getX());
			nbt.putInt("secondTriggeredBlockPositionOffsetY", this.secondTriggeredBlock.getLeft().getY());
			nbt.putInt("secondTriggeredBlockPositionOffsetZ", this.secondTriggeredBlock.getLeft().getZ());
			nbt.putBoolean("secondTriggeredBlockResets", this.secondTriggeredBlock.getRight());
		} else {
			nbt.remove("secondTriggeredBlockPositionOffsetX");
			nbt.remove("secondTriggeredBlockPositionOffsetY");
			nbt.remove("secondTriggeredBlockPositionOffsetZ");
			nbt.remove("secondTriggeredBlockResets");
		}

		if (this.isWriting) {
			nbt.putBoolean("isWriting", true);
		} else {
			nbt.remove("isWriting");
		}

		if (!this.dataIdentifier.isEmpty()) {
			nbt.putString("dataIdentifier", this.dataIdentifier);
		} else {
			nbt.remove("dataIdentifier");
		}

		if (this.comparedDataValue != 0) {
			nbt.putInt("dataValueThreshold", this.comparedDataValue);
		} else {
			nbt.remove("dataValueThreshold");
		}

		if (this.dataReadingMode != DataReadingMode.LESSER) {
			nbt.putString("dataReadingMode", this.dataReadingMode.name);
		} else {
			nbt.remove("dataReadingMode");
		}

		if (this.isAdding) {
			nbt.putBoolean("isAdding", true);
		} else {
			nbt.remove("isAdding");
		}

		if (this.newDataValue != 0) {
			nbt.putInt("newDataValue", this.newDataValue);
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

		if (nbt.contains("firstTriggeredBlockPositionOffsetX", NbtElement.INT_TYPE) && nbt.contains("firstTriggeredBlockPositionOffsetY", NbtElement.INT_TYPE) && nbt.contains("firstTriggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) && nbt.contains("firstTriggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.firstTriggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("firstTriggeredBlockPositionOffsetZ"), -48, 48)
			), nbt.getBoolean("firstTriggeredBlockResets"));
		} else {
			this.firstTriggeredBlock = new MutablePair<>(FIRST_TRIGGERED_BLOCK_POS_DEFAULT, false);
		}

		if (nbt.contains("secondTriggeredBlockPositionOffsetX", NbtElement.INT_TYPE) && nbt.contains("secondTriggeredBlockPositionOffsetY", NbtElement.INT_TYPE) && nbt.contains("secondTriggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) && nbt.contains("secondTriggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.secondTriggeredBlock = new MutablePair<>(new BlockPos(
					MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("secondTriggeredBlockPositionOffsetZ"), -48, 48)
			), nbt.getBoolean("secondTriggeredBlockResets"));
		} else {
			this.secondTriggeredBlock = new MutablePair<>(SECOND_TRIGGERED_BLOCK_POS_DEFAULT, false);
		}

		if (nbt.contains("isWriting", NbtElement.BYTE_TYPE)) {
			this.isWriting = nbt.getBoolean("isWriting");
		} else {
			this.isWriting = false;
		}

		if (nbt.contains("dataIdentifier", NbtElement.STRING_TYPE)) {
			this.dataIdentifier = nbt.getString("dataIdentifier");
		} else {
			this.dataIdentifier = "";
		}

		if (nbt.contains("dataValueThreshold", NbtElement.INT_TYPE)) {
			this.comparedDataValue = nbt.getInt("dataValueThreshold");
		} else {
			this.comparedDataValue = 0;
		}

		if (nbt.contains("dataReadingMode", NbtElement.STRING_TYPE)) {
			this.dataReadingMode = DataReadingMode.byName(nbt.getString("dataReadingMode")).orElse(DataReadingMode.LESSER);
		} else {
			this.dataReadingMode = DataReadingMode.LESSER;
		}

		if (nbt.contains("isAdding", NbtElement.BYTE_TYPE)) {
			this.isAdding = nbt.getBoolean("isAdding");
		} else {
			this.isAdding = false;
		}

		if (nbt.contains("newDataValue", NbtElement.INT_TYPE)) {
			this.newDataValue = nbt.getInt("newDataValue");
		} else {
			this.newDataValue = 0;
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

	public BlockPos getDataProvidingBlockPosOffset() {
		return dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public MutablePair<BlockPos, Boolean> getFirstTriggeredBlock() {
		return this.firstTriggeredBlock;
	}

	public void setFirstTriggeredBlock(MutablePair<BlockPos, Boolean> firstTriggeredBlock) {
		this.firstTriggeredBlock = firstTriggeredBlock;
	}

	public MutablePair<BlockPos, Boolean> getSecondTriggeredBlock() {
		return this.secondTriggeredBlock;
	}

	public void setSecondTriggeredBlock(MutablePair<BlockPos, Boolean> secondTriggeredBlock) {
		this.secondTriggeredBlock = secondTriggeredBlock;
	}

	public boolean isWriting() {
		return isWriting;
	}

	public void setIsWriting(boolean isWriting) {
		this.isWriting = isWriting;
	}

	public String getDataIdentifier() {
		return this.dataIdentifier;
	}

	public void setDataIdentifier(String dataIdentifier) {
		this.dataIdentifier = dataIdentifier;
	}

	public int getComparedDataValue() {
		return this.comparedDataValue;
	}

	public void setComparedDataValue(int comparedDataValue) {
		this.comparedDataValue = comparedDataValue;
	}

	public DataReadingMode getDataReadingMode() {
		return this.dataReadingMode;
	}

	public void setDataReadingMode(DataReadingMode dataReadingMode) {
		this.dataReadingMode = dataReadingMode;
	}

	public boolean isAdding() {
		return this.isAdding;
	}

	public void setIsAdding(boolean isAdding) {
		this.isAdding = isAdding;
	}

	public int getNewDataValue() {
		return this.newDataValue;
	}

	public void setNewDataValue(int newDataValue) {
		this.newDataValue = newDataValue;
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			String worldName = this.world.getRegistryKey().getValue().getPath();
			MinecraftServer server = this.world.getServer();
			BlockPos dataProviderBlockPos = new BlockPos(this.pos.getX() + this.dataProvidingBlockPosOffset.getX(), this.pos.getY() + this.dataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.dataProvidingBlockPosOffset.getZ());

			BlockEntity blockEntity = world.getBlockEntity(dataProviderBlockPos);
			if (blockEntity instanceof ProvidesData providesDataEntity) {
				if (this.isWriting) {
					if (this.isAdding) {
						providesDataEntity.addData(this.dataIdentifier, this.newDataValue);
					} else {
						providesDataEntity.setData(this.dataIdentifier, this.newDataValue);
					}
				} else {
					int currentValue = providesDataEntity.getData(this.dataIdentifier);
					boolean bl = (this.dataReadingMode == DataReadingMode.LESSER && currentValue < this.comparedDataValue) || (this.dataReadingMode == DataReadingMode.GREATER && currentValue > this.comparedDataValue) || (this.dataReadingMode == DataReadingMode.EQUAL && currentValue == this.comparedDataValue);
					BlockPos triggeredBlockPos = BlockPos.ORIGIN;
					boolean triggeredBlockResets = false;
					if (bl) {
						if (this.firstTriggeredBlock.getLeft() != BlockPos.ORIGIN) {
							triggeredBlockPos = new BlockPos(this.pos.getX() + this.firstTriggeredBlock.getLeft().getX(), this.pos.getY() + this.firstTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.firstTriggeredBlock.getLeft().getZ());
							triggeredBlockResets = this.firstTriggeredBlock.getRight();
						}
					} else {
						if (this.secondTriggeredBlock.getLeft() != BlockPos.ORIGIN) {
							triggeredBlockPos = new BlockPos(this.pos.getX() + this.secondTriggeredBlock.getLeft().getX(), this.pos.getY() + this.secondTriggeredBlock.getLeft().getY(), this.pos.getZ() + this.secondTriggeredBlock.getLeft().getZ());
							triggeredBlockResets = this.secondTriggeredBlock.getRight();
						}
					}
					if (triggeredBlockPos != BlockPos.ORIGIN) {
						BlockEntity blockEntity1 = world.getBlockEntity(triggeredBlockPos);
						if (blockEntity1 != this) {
							if (triggeredBlockResets && blockEntity1 instanceof Resetable resetable) {
								resetable.reset();
							} else if (!triggeredBlockResets && blockEntity1 instanceof Triggerable triggerable) {
								triggerable.trigger();
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				this.dataProvidingBlockPosOffset = BlockRotationUtils.rotateOffsetBlockPos(this.dataProvidingBlockPosOffset, blockRotation);

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.firstTriggeredBlock.getLeft(), blockRotation));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.secondTriggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.FRONT_BACK);

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				this.dataProvidingBlockPosOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.dataProvidingBlockPosOffset, BlockMirror.LEFT_RIGHT);

				this.firstTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.firstTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.secondTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.secondTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

	public static enum DataReadingMode implements StringIdentifiable {
		LESSER("lesser"),
		EQUAL("equal"),
		GREATER("greater");

		private final String name;

		private DataReadingMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<DataReadingMode> byName(String name) {
			return Arrays.stream(DataReadingMode.values()).filter(screenPage -> screenPage.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.data_access_block.data_reading_mode." + this.name);
		}
	}
}
