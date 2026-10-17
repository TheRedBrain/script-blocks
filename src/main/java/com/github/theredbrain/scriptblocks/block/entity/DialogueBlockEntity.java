package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
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
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtList nbtList = new NbtList();
		this.useRelayBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("key", key);
			nbtCompound.put("position_offset", NbtHelper.fromBlockPos(entry));
			nbtList.add(nbtCompound.copy());
		});
		nbt.put("use_relay_blocks", nbtList.copy());

		nbtList.clear();
		this.triggeredBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("key", key);
			nbtCompound.put("position_offset", NbtHelper.fromBlockPos(entry.getLeft()));
			nbtCompound.putBoolean("resets", entry.getRight());
			nbtList.add(nbtCompound.copy());
		});
		nbt.put("triggered_blocks", nbtList.copy());

		nbt.put("data_providing_block_position_offset", NbtHelper.fromBlockPos(this.dataProvidingBlockPosOffset));

		nbtList.clear();
		for (String identifier : this.startingDialogueList) {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putString("identifier", identifier);
			nbtList.add(nbtCompound.copy());
		}
		nbt.put("starting_dialogues", nbtList);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.useRelayBlocks.clear();
		if (nbt.contains("dialogueUsedBlocksKeysSize")) {
			int dialogueUsedBlocksKeysSize = nbt.getInt("dialogueUsedBlocksKeysSize");
			for (int i = 0; i < dialogueUsedBlocksKeysSize; i++) {
				this.useRelayBlocks.put(nbt.getString("dialogueUsedBlocks_key_" + i), new BlockPos(
						MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_X_" + i), -48, 48),
						MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Y_" + i), -48, 48),
						MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Z_" + i), -48, 48)
				));
				nbt.remove("dialogueUsedBlocks_key_" + i);
				nbt.remove("dialogueUsedBlocks_entry_X_" + i);
				nbt.remove("dialogueUsedBlocks_entry_Y_" + i);
				nbt.remove("dialogueUsedBlocks_entry_Z_" + i);
			}
			nbt.remove("dialogueUsedBlocksKeysSize");
		} else {
			NbtList nbtList = nbt.getList("use_relay_blocks", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbtCompound = nbtList.getCompound(i);
				this.useRelayBlocks.put(
						nbtCompound.getString("key"),
						BlockPos.max(
								BlockPos.min(
										NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
										new BlockPos(48, 48, 48)
								),
								new BlockPos(-48, -48, -48)
						)
				);
			}
		}

		this.triggeredBlocks.clear();
		if (nbt.contains("dialogueTriggeredBlocksKeysSize")) {
			int dialogueTriggeredBlocksKeysSize = nbt.getInt("dialogueTriggeredBlocksKeysSize");
			for (int i = 0; i < dialogueTriggeredBlocksKeysSize; i++) {
				this.triggeredBlocks.put(nbt.getString("dialogueTriggeredBlocks_key_" + i), new MutablePair<>(
						new BlockPos(
								MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_X_" + i), -48, 48),
								MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Y_" + i), -48, 48),
								MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Z_" + i), -48, 48)
						), nbt.getBoolean("dialogueTriggeredBlocks_entry_resets_" + i)));
				nbt.remove("dialogueTriggeredBlocks_key_" + i);
				nbt.remove("dialogueTriggeredBlocks_entry_X_" + i);
				nbt.remove("dialogueTriggeredBlocks_entry_Y_" + i);
				nbt.remove("dialogueTriggeredBlocks_entry_Z_" + i);
				nbt.remove("dialogueTriggeredBlocks_entry_resets_" + i);
			}
			nbt.remove("dialogueTriggeredBlocksKeysSize");
		} else {
			NbtList nbtList = nbt.getList("triggered_blocks", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbtCompound = nbtList.getCompound(i);
				this.triggeredBlocks.put(
						nbtCompound.getString("key"),
						new MutablePair<>(
								BlockPos.max(
										BlockPos.min(
												NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
												new BlockPos(48, 48, 48)
										),
										new BlockPos(-48, -48, -48)
								),
								nbtCompound.getBoolean("resets")
						)
				);
			}
		}

		if (nbt.contains("dataBlockOffsetX") || nbt.contains("dataBlockOffsetY") || nbt.contains("dataBlockOffsetZ")) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataBlockOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataBlockOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataBlockOffsetZ"), -48, 48)
			);
			nbt.remove("dataBlockOffsetX");
			nbt.remove("dataBlockOffsetY");
			nbt.remove("dataBlockOffsetZ");
		} else {
			this.dataProvidingBlockPosOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "data_providing_block_pos_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		this.startingDialogueList.clear();
		if (nbt.contains("startingDialogueListSize")) {
			int startingDialogueListSize = nbt.getInt("startingDialogueListSize");
			for (int i = 0; i < startingDialogueListSize; i++) {
				this.startingDialogueList.add(nbt.getString("startingDialogueList_name_" + i));
				nbt.remove("startingDialogueList_name_" + i);
			}
			nbt.remove("startingDialogueListSize");
		} else {
			NbtList nbtList = nbt.getList("starting_dialogues", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbtCompound = nbtList.getCompound(i);
				this.startingDialogueList.add(
						nbtCompound.getString("identifier")
				);
			}
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
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
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
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
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
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
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
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
