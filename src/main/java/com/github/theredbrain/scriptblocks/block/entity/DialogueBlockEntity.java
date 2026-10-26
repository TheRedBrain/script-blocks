package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DialogueBlockEntity extends RotatedBlockEntity implements DialogueAnchor {

	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;
	private final HashMap<String, BlockPos> useRelayBlocks = new HashMap<>();
	private final HashMap<String, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
	private final List<String> startingDialogueList = new ArrayList<>();

	public DialogueBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.DIALOGUE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("use_relay_blocks", NbtCompound.CODEC);
		this.useRelayBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("key", key);
			nbtCompound.put("position_offset", BlockPos.CODEC, entry);
			listAppender.add(nbtCompound.copy());
		});

		WriteView.ListAppender<NbtCompound> listAppender1 = view.getListAppender("triggered_blocks", NbtCompound.CODEC);
		this.triggeredBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("key", key);
			nbtCompound.put("position_offset", BlockPos.CODEC, entry.getLeft());
			nbtCompound.putBoolean("resets", entry.getRight());
			listAppender1.add(nbtCompound.copy());
		});

		view.put("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		WriteView.ListAppender<NbtCompound> listAppender2 = view.getListAppender("starting_dialogues", NbtCompound.CODEC);
		for (String identifier : this.startingDialogueList) {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("identifier", identifier);
			listAppender2.add(nbtCompound.copy());
		}

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.useRelayBlocks.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("use_relay_blocks", NbtCompound.CODEC)) {
			this.useRelayBlocks.put(
					nbtCompound.getString("key", ""),
					BlockPos.max(
							BlockPos.min(
									nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					)
			);
		}

		this.triggeredBlocks.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("triggered_blocks", NbtCompound.CODEC)) {
			this.triggeredBlocks.put(
					nbtCompound.getString("key", ""),
					new MutablePair<>(
							BlockPos.max(
									BlockPos.min(
											nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
											new BlockPos(48, 48, 48)
									),
									new BlockPos(-48, -48, -48)
							),
							nbtCompound.getBoolean("resets", false)
					)
			);
		}

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.startingDialogueList.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("starting_dialogues", NbtCompound.CODEC)) {
			this.startingDialogueList.add(
					nbtCompound.getString("identifier", "")
			);
		}

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
		return new BlockPos(this.pos.add(this.dataProvidingBlockPosOffset));
	}

	@Override
	public List<MutablePair<String, BlockPos>> getDialogueUsedBlocks() {
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = new ArrayList<>();
		List<String> dialogueUsedBlocksKeys = new ArrayList<>(this.useRelayBlocks.keySet());
		for (String key : dialogueUsedBlocksKeys) {
			dialogueUsedBlocks.add(new MutablePair<>(key, this.pos.add(this.useRelayBlocks.get(key))));
		}
		return dialogueUsedBlocks;
	}

	@Override
	public List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks() {
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = new ArrayList<>();
		List<String> dialogueTriggeredBlocksKeys = new ArrayList<>(this.triggeredBlocks.keySet());
		for (String key : dialogueTriggeredBlocksKeys) {
			dialogueTriggeredBlocks.add(new MutablePair<>(key, new MutablePair<>(this.pos.add(this.triggeredBlocks.get(key).getLeft()), this.triggeredBlocks.get(key).getRight())));
		}
		return dialogueTriggeredBlocks;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
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
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				List<String> keys = new ArrayList<>(this.useRelayBlocks.keySet());
				for (String key : keys) {
					this.useRelayBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.FRONT_BACK));
				}
				keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				List<String> keys = new ArrayList<>(this.useRelayBlocks.keySet());
				for (String key : keys) {
					this.useRelayBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.LEFT_RIGHT));
				}
				keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
