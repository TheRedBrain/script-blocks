package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ItemUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class RelayTriggerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private SelectionMode selectionMode = SelectionMode.LIST;
	private boolean showArea = false;
	private boolean resetsArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());
	private TriggerMode triggerMode = TriggerMode.NORMAL;
	private boolean isTriggerAmountDataDriven = true;
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
	private String dataIdentifier = "";
	private int triggerAmount = 1;

	public RelayTriggerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.RELAY_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putString("selection_mode", this.selectionMode.getSerializedName());

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.putBoolean("is_reset", this.resetsArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("triggered_blocks", CompoundTag.CODEC);
		for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
			nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, triggeredBlock.getLeft().getLeft());
			nbtCompound.putBoolean("resets", triggeredBlock.getLeft().getRight());
			nbtCompound.putInt("chance", triggeredBlock.getRight());
			listAppender.add(nbtCompound.copy());
		}

		view.putString("trigger_mode", this.triggerMode.getSerializedName());

		view.putBoolean("is_trigger_amount_data_driven", this.isTriggerAmountDataDriven);

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("data_identifier", this.dataIdentifier);

		view.putInt("trigger_amount", this.triggerAmount);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.selectionMode = SelectionMode.byName(view.getStringOr("selection_mode", "")).orElseGet(() -> SelectionMode.LIST);

		CompoundTag nbtCompound = view.read("area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showArea = nbtCompound.getBooleanOr("visible", false);

		this.resetsArea = nbtCompound.getBooleanOr("is_reset", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.triggeredBlocks.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("triggered_blocks", CompoundTag.CODEC)) {
			this.triggeredBlocks.add(
					new MutablePair<>(
							new MutablePair<>(
									nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									nbtCompound1.getBooleanOr("resets", false)
							),
							Mth.clamp(nbtCompound1.getIntOr("chance", 1), 0, 100)
					)
			);
		}

		this.triggerMode = TriggerMode.byName(view.getStringOr("trigger_mode", "")).orElseGet(() -> TriggerMode.NORMAL);

		this.isTriggerAmountDataDriven = view.getBooleanOr("is_trigger_amount_data_driven", true);

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.dataIdentifier = view.getStringOr("data_identifier", "");

		this.triggerAmount = view.getIntOr("trigger_amount", 1);

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
	public SelectionMode getSelectionMode() {
		return this.selectionMode;
	}

	public void setSelectionMode(SelectionMode selectionMode) {
		this.selectionMode = selectionMode;
	}

	public boolean getShowArea() {
		return this.showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public boolean getResetsArea() {
		return this.resetsArea;
	}

	public void setResetsArea(boolean resetsArea) {
		this.resetsArea = resetsArea;
	}

	public Vec3i getAreaDimensions() {
		return this.areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
	}

	public BlockPos getAreaPositionOffset() {
		return this.areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
	}

	public List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> getTriggeredBlocks() {
		return this.triggeredBlocks;
	}

	public void setTriggeredBlocks(List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks) {
		this.triggeredBlocks.clear();
		this.triggeredBlocks.addAll(triggeredBlocks);
	}

	public TriggerMode getTriggerMode() {
		return this.triggerMode;
	}

	public void setTriggerMode(TriggerMode triggerMode) {
		this.triggerMode = triggerMode;
	}

	public boolean isTriggerAmountDataDriven() {
		return this.isTriggerAmountDataDriven;
	}

	public void setIsTriggerAmountDataDriven(boolean isTriggerAmountDataDriven) {
		this.isTriggerAmountDataDriven = isTriggerAmountDataDriven;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
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

	public int getTriggerAmount() {
		return this.triggerAmount;
	}

	public void setTriggerAmount(int triggerAmount) {
		this.triggerAmount = triggerAmount;
	}
	// endregion --- getter & setter ---

	private int getActualTriggerAmount() {
		if (this.level != null && this.isTriggerAmountDataDriven && this.dataProvidingBlockPosOffset != BlockPos.ZERO && !this.dataIdentifier.isEmpty()) {
			BlockPos dataProviderBlockPos = new BlockPos(this.worldPosition.getX() + this.dataProvidingBlockPosOffset.getX(), this.worldPosition.getY() + this.dataProvidingBlockPosOffset.getY(), this.worldPosition.getZ() + this.dataProvidingBlockPosOffset.getZ());
			BlockEntity blockEntity = level.getBlockEntity(dataProviderBlockPos);
			if (blockEntity instanceof ProvidesData providesDataEntity) {
				String data = providesDataEntity.getData(this.dataIdentifier);
				return ItemUtils.parseInt(data);
			}
		}
		return this.triggerAmount;
	}

	@Override
	public void trigger() {
		if (this.level != null) {
			BlockEntity blockEntity;
			if (this.selectionMode == SelectionMode.LIST) {
				if (this.triggerMode == TriggerMode.NORMAL) {
					for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
						BlockPos triggeredBlockPos = triggeredBlock.left.left;
						blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + triggeredBlockPos.getX(), this.worldPosition.getY() + triggeredBlockPos.getY(), this.worldPosition.getZ() + triggeredBlockPos.getZ()));
						if (blockEntity == this) {
							continue;
						}
						if (triggeredBlock.getLeft().getRight()) {
							if (blockEntity instanceof Resetable resetable) {
								resetable.reset();
							}
						} else {
							if (blockEntity instanceof Triggerable triggerable) {
								triggerable.trigger();
							}
						}
					}
				} else if (this.triggerMode == TriggerMode.RANDOM) {
					for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
						int chance = this.level.getRandom().nextInt(100);
						if (chance <= triggeredBlock.right) {
							BlockPos triggeredBlockPos = triggeredBlock.left.left;
							blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + triggeredBlockPos.getX(), this.worldPosition.getY() + triggeredBlockPos.getY(), this.worldPosition.getZ() + triggeredBlockPos.getZ()));
							if (blockEntity == this) {
								continue;
							}
							if (triggeredBlock.getLeft().getRight()) {
								if (blockEntity instanceof Resetable resetable) {
									resetable.reset();
								}
							} else {
								if (blockEntity instanceof Triggerable triggerable) {
									triggerable.trigger();
								}
							}
						}
					}
				} else if (this.triggerMode == TriggerMode.BINOMIAL) {
					int totalAmount = 0;
					for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
						totalAmount += triggeredBlock.getRight();
					}
					if (totalAmount > 0) {
						int triggerAmount = this.getActualTriggerAmount();
						for (int i = 0; i < triggerAmount; i++) {
							int pickedChoice = this.level.getRandom().nextInt(totalAmount);
							for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
								pickedChoice -= triggeredBlock.getRight();
								if (pickedChoice <= 0) {
									BlockPos triggeredBlockPos = triggeredBlock.left.left;
									blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + triggeredBlockPos.getX(), this.worldPosition.getY() + triggeredBlockPos.getY(), this.worldPosition.getZ() + triggeredBlockPos.getZ()));
									if (blockEntity != this) {
										if (triggeredBlock.getLeft().getRight()) {
											if (blockEntity instanceof Resetable resetable) {
												resetable.reset();
											}
										} else {
											if (blockEntity instanceof Triggerable triggerable) {
												triggerable.trigger();
											}
										}
									}
									break;
								}
							}
						}
					}
				} else if (this.triggerMode == TriggerMode.HYPER_GEOMETRIC) {
					List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> list = new ArrayList<>(this.triggeredBlocks);
					int triggerAmount = this.getActualTriggerAmount();
					for (int i = 0; i < triggerAmount; i++) {
						int totalAmount = 0;
						for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : list) {
							totalAmount += triggeredBlock.getRight();
						}
						if (totalAmount > 0) {
							int pickedChoice = this.level.getRandom().nextInt(totalAmount);
							for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : list) {
								pickedChoice -= triggeredBlock.getRight();
								if (pickedChoice <= 0) {
									BlockPos triggeredBlockPos = triggeredBlock.left.left;
									blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + triggeredBlockPos.getX(), this.worldPosition.getY() + triggeredBlockPos.getY(), this.worldPosition.getZ() + triggeredBlockPos.getZ()));
									if (blockEntity != this) {
										if (triggeredBlock.getLeft().getRight()) {
											if (blockEntity instanceof Resetable resetable) {
												resetable.reset();
											}
										} else {
											if (blockEntity instanceof Triggerable triggerable) {
												triggerable.trigger();
											}
										}
									}
									list.remove(triggeredBlock);
									break;
								}
							}
						}
					}
				}
			} else if (this.selectionMode == SelectionMode.AREA) {
				Vec3i activationAreaDimensions = this.getAreaDimensions();
				BlockPos blockPos = new BlockPos(this.worldPosition.getX() + this.areaPositionOffset.getX(), this.worldPosition.getY() + this.areaPositionOffset.getY(), this.worldPosition.getZ() + this.areaPositionOffset.getZ());
				for (int i = 0; i < activationAreaDimensions.getX(); i++) {
					for (int j = 0; j < activationAreaDimensions.getY(); j++) {
						for (int k = 0; k < activationAreaDimensions.getZ(); k++) {
							blockEntity = level.getBlockEntity(new BlockPos(blockPos.getX() + i, blockPos.getY() + j, blockPos.getZ() + k));
							if (blockEntity == this) {
								continue;
							}
							if (this.resetsArea) {
								if (blockEntity instanceof Resetable resetable) {
									resetable.reset();
								}
							} else {
								if (blockEntity instanceof Triggerable triggerable) {
									triggerable.trigger();
								}
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock.getLeft().getLeft(), blockRotation), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), Mirror.FRONT_BACK), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), Mirror.LEFT_RIGHT), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public static enum SelectionMode implements StringRepresentable {
		LIST("list"),
		AREA("area");

		private final String name;

		private SelectionMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<SelectionMode> byName(String name) {
			return Arrays.stream(SelectionMode.values()).filter(selectionMode -> selectionMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.relay_trigger_block.selection_mode." + this.name);
		}
	}

	public static enum TriggerMode implements StringRepresentable {
		NORMAL("normal"),
		RANDOM("random"),
		BINOMIAL("binomial"),
		HYPER_GEOMETRIC("hyper_geometric");

		private final String name;

		private TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public static Optional<TriggerMode> byName(String name) {
			return Arrays.stream(TriggerMode.values()).filter(triggerMode -> triggerMode.getSerializedName().equals(name)).findFirst();
		}

		public Component asText() {
			return Component.translatable("gui.relay_trigger_block.trigger_mode." + this.name);
		}
	}
}
