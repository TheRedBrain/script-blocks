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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RelayTriggerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private SelectionMode selectionMode = SelectionMode.LIST;
	private boolean showArea = false;
	private boolean resetsArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

	private final List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(List.of());
	private TriggerMode triggerMode = TriggerMode.NORMAL;
	private boolean isTriggerAmountDataDriven = true;
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private String dataIdentifier = "";
	private int triggerAmount = 1;

	public RelayTriggerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.RELAY_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		view.putString("selection_mode", this.selectionMode.asString());

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.putBoolean("is_reset", this.resetsArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.put("area", NbtCompound.CODEC, nbtCompound.copy());

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("triggered_blocks", NbtCompound.CODEC);
		for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
			nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", BlockPos.CODEC, triggeredBlock.getLeft().getLeft());
			nbtCompound.putBoolean("resets", triggeredBlock.getLeft().getRight());
			nbtCompound.putInt("chance", triggeredBlock.getRight());
			listAppender.add(nbtCompound.copy());
		}

		view.putString("trigger_mode", this.triggerMode.asString());

		view.putBoolean("is_trigger_amount_data_driven", this.isTriggerAmountDataDriven);

		view.put("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putString("data_identifier", this.dataIdentifier);

		view.putInt("trigger_amount", this.triggerAmount);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.selectionMode = SelectionMode.byName(view.getString("selection_mode", "")).orElseGet(() -> SelectionMode.LIST);

		NbtCompound nbtCompound = view.read("area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showArea = nbtCompound.getBoolean("visible", false);

		this.resetsArea = nbtCompound.getBoolean("is_reset", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.triggeredBlocks.clear();
		for (NbtCompound nbtCompound1 : view.getTypedListView("triggered_blocks", NbtCompound.CODEC)) {
			this.triggeredBlocks.add(
					new MutablePair<>(
							new MutablePair<>(
									nbtCompound1.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
									nbtCompound1.getBoolean("resets", false)
							),
							MathHelper.clamp(nbtCompound1.getInt("chance", 1), 0, 100)
					)
			);
		}

		this.triggerMode = TriggerMode.byName(view.getString("trigger_mode", "")).orElseGet(() -> TriggerMode.NORMAL);

		this.isTriggerAmountDataDriven = view.getBoolean("is_trigger_amount_data_driven", true);

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.dataIdentifier = view.getString("data_identifier", "");

		this.triggerAmount = view.getInt("trigger_amount", 1);

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
		if (this.world != null && this.isTriggerAmountDataDriven && this.dataProvidingBlockPosOffset != BlockPos.ORIGIN && !this.dataIdentifier.isEmpty()) {
			BlockPos dataProviderBlockPos = new BlockPos(this.pos.getX() + this.dataProvidingBlockPosOffset.getX(), this.pos.getY() + this.dataProvidingBlockPosOffset.getY(), this.pos.getZ() + this.dataProvidingBlockPosOffset.getZ());
			BlockEntity blockEntity = world.getBlockEntity(dataProviderBlockPos);
			if (blockEntity instanceof ProvidesData providesDataEntity) {
				String data = providesDataEntity.getData(this.dataIdentifier);
				return ItemUtils.parseInt(data);
			}
		}
		return this.triggerAmount;
	}

	@Override
	public void trigger() {
		if (this.world != null) {
			BlockEntity blockEntity;
			if (this.selectionMode == SelectionMode.LIST) {
				if (this.triggerMode == TriggerMode.NORMAL) {
					for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
						BlockPos triggeredBlockPos = triggeredBlock.left.left;
						blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + triggeredBlockPos.getX(), this.pos.getY() + triggeredBlockPos.getY(), this.pos.getZ() + triggeredBlockPos.getZ()));
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
						int chance = this.world.random.nextInt(100);
						if (chance <= triggeredBlock.right) {
							BlockPos triggeredBlockPos = triggeredBlock.left.left;
							blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + triggeredBlockPos.getX(), this.pos.getY() + triggeredBlockPos.getY(), this.pos.getZ() + triggeredBlockPos.getZ()));
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
							int pickedChoice = this.world.random.nextInt(totalAmount);
							for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
								pickedChoice -= triggeredBlock.getRight();
								if (pickedChoice <= 0) {
									BlockPos triggeredBlockPos = triggeredBlock.left.left;
									blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + triggeredBlockPos.getX(), this.pos.getY() + triggeredBlockPos.getY(), this.pos.getZ() + triggeredBlockPos.getZ()));
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
							int pickedChoice = this.world.random.nextInt(totalAmount);
							for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : list) {
								pickedChoice -= triggeredBlock.getRight();
								if (pickedChoice <= 0) {
									BlockPos triggeredBlockPos = triggeredBlock.left.left;
									blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + triggeredBlockPos.getX(), this.pos.getY() + triggeredBlockPos.getY(), this.pos.getZ() + triggeredBlockPos.getZ()));
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
				BlockPos blockPos = new BlockPos(this.pos.getX() + this.areaPositionOffset.getX(), this.pos.getY() + this.areaPositionOffset.getY(), this.pos.getZ() + this.areaPositionOffset.getZ());
				for (int i = 0; i < activationAreaDimensions.getX(); i++) {
					for (int j = 0; j < activationAreaDimensions.getY(); j++) {
						for (int k = 0; k < activationAreaDimensions.getZ(); k++) {
							blockEntity = world.getBlockEntity(new BlockPos(blockPos.getX() + i, blockPos.getY() + j, blockPos.getZ() + k));
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.rotateOffsetBlockPos(triggeredBlock.getLeft().getLeft(), blockRotation), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.FRONT_BACK), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> newTriggeredBlocks = new ArrayList<>(List.of());
				for (MutablePair<MutablePair<BlockPos, Boolean>, Integer> triggeredBlock : this.triggeredBlocks) {
					newTriggeredBlocks.add(new MutablePair<>(new MutablePair<>(BlockRotationUtils.mirrorOffsetBlockPos(triggeredBlock.getLeft().getLeft(), BlockMirror.LEFT_RIGHT), triggeredBlock.getLeft().getRight()), triggeredBlock.getRight()));
				}
				this.triggeredBlocks.clear();
				this.triggeredBlocks.addAll(newTriggeredBlocks);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

	public static enum SelectionMode implements StringIdentifiable {
		LIST("list"),
		AREA("area");

		private final String name;

		private SelectionMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<SelectionMode> byName(String name) {
			return Arrays.stream(SelectionMode.values()).filter(selectionMode -> selectionMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.relay_trigger_block.selection_mode." + this.name);
		}
	}

	public static enum TriggerMode implements StringIdentifiable {
		NORMAL("normal"),
		RANDOM("random"),
		BINOMIAL("binomial"),
		HYPER_GEOMETRIC("hyper_geometric");

		private final String name;

		private TriggerMode(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}

		public static Optional<TriggerMode> byName(String name) {
			return Arrays.stream(TriggerMode.values()).filter(triggerMode -> triggerMode.asString().equals(name)).findFirst();
		}

		public Text asText() {
			return Text.translatable("gui.relay_trigger_block.trigger_mode." + this.name);
		}
	}
}
