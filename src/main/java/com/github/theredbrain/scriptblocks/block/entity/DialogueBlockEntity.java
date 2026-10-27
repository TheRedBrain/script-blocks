package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DialogueBlockEntity extends RotatedBlockEntity implements DialogueAnchor {

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;
	private final HashMap<String, BlockPos> useRelayBlocks = new HashMap<>();
	private final HashMap<String, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
	private final List<String> startingDialogueList = new ArrayList<>();

	public DialogueBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.DIALOGUE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("use_relay_blocks", CompoundTag.CODEC);
		this.useRelayBlocks.forEach((key, entry) -> {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putString("key", key);
			nbtCompound.store("position_offset", BlockPos.CODEC, entry);
			listAppender.add(nbtCompound.copy());
		});

		ValueOutput.TypedOutputList<CompoundTag> listAppender1 = view.list("triggered_blocks", CompoundTag.CODEC);
		this.triggeredBlocks.forEach((key, entry) -> {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putString("key", key);
			nbtCompound.store("position_offset", BlockPos.CODEC, entry.getLeft());
			nbtCompound.putBoolean("resets", entry.getRight());
			listAppender1.add(nbtCompound.copy());
		});

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		ValueOutput.TypedOutputList<CompoundTag> listAppender2 = view.list("starting_dialogues", CompoundTag.CODEC);
		for (String identifier : this.startingDialogueList) {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putString("identifier", identifier);
			listAppender2.add(nbtCompound.copy());
		}

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.useRelayBlocks.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("use_relay_blocks", CompoundTag.CODEC)) {
			this.useRelayBlocks.put(
					nbtCompound.getStringOr("key", ""),
					BlockPos.max(
							BlockPos.min(
									nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					)
			);
		}

		this.triggeredBlocks.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("triggered_blocks", CompoundTag.CODEC)) {
			this.triggeredBlocks.put(
					nbtCompound.getStringOr("key", ""),
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							nbtCompound.getBooleanOr("resets", false)
					)
			);
		}

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.startingDialogueList.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("starting_dialogues", CompoundTag.CODEC)) {
			this.startingDialogueList.add(
					nbtCompound.getStringOr("identifier", "")
			);
		}

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
	public HashMap<String, BlockPos> getUseRelayBlocks() {
		return this.useRelayBlocks;
	}

	public void setUseRelayBlocks(HashMap<String, BlockPos> dialogueUsedBlocks) {
		this.useRelayBlocks.clear();
		this.useRelayBlocks.putAll(dialogueUsedBlocks);
	}

	public HashMap<String, MutablePair<BlockPos, Boolean>> getTriggeredBlocks() {
		return this.triggeredBlocks;
	}

	public void setTriggeredBlocks(HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocks) {
		this.triggeredBlocks.clear();
		this.triggeredBlocks.putAll(dialogueTriggeredBlocks);
	}

	public List<String> getStartingDialogueList() {
		return this.startingDialogueList;
	}

	public void setStartingDialogueList(List<String> startingDialogueList) {
		this.startingDialogueList.clear();
		this.startingDialogueList.addAll(startingDialogueList);
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return this.dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}
	// endregion --- getter & setter ---

	@Override
	public BlockPos getDataBlockPos() {
		return new BlockPos(this.worldPosition.offset(this.dataProvidingBlockPosOffset));
	}

	@Override
	public List<MutablePair<String, BlockPos>> getDialogueUsedBlocks() {
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = new ArrayList<>();
		List<String> dialogueUsedBlocksKeys = new ArrayList<>(this.useRelayBlocks.keySet());
		for (String key : dialogueUsedBlocksKeys) {
			dialogueUsedBlocks.add(new MutablePair<>(key, this.worldPosition.offset(this.useRelayBlocks.get(key))));
		}
		return dialogueUsedBlocks;
	}

	@Override
	public List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks() {
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = new ArrayList<>();
		List<String> dialogueTriggeredBlocksKeys = new ArrayList<>(this.triggeredBlocks.keySet());
		for (String key : dialogueTriggeredBlocksKeys) {
			dialogueTriggeredBlocks.add(new MutablePair<>(key, new MutablePair<>(this.worldPosition.offset(this.triggeredBlocks.get(key).getLeft()), this.triggeredBlocks.get(key).getRight())));
		}
		return dialogueTriggeredBlocks;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				List<String> keys = new ArrayList<>(this.useRelayBlocks.keySet());
				for (String key : keys) {
					this.useRelayBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos, blockRotation));
				}
				keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				List<String> keys = new ArrayList<>(this.useRelayBlocks.keySet());
				for (String key : keys) {
					this.useRelayBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, Mirror.FRONT_BACK));
				}
				keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.FRONT_BACK));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				List<String> keys = new ArrayList<>(this.useRelayBlocks.keySet());
				for (String key : keys) {
					this.useRelayBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, Mirror.LEFT_RIGHT));
				}
				keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.LEFT_RIGHT));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
