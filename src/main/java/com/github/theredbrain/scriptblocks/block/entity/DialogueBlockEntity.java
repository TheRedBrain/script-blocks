package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.registry.DialoguesRegistry;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DialogueBlockEntity extends RotatedBlockEntity implements DialogueAnchor {

	private HashMap<String, BlockPos> dialogueUsedBlocksMap = new HashMap<>();
	private HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocksMap = new HashMap<>();
	private List<String> startingDialogueList = new ArrayList<>();

	public DialogueBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DIALOGUE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		List<String> dialogueUsedBlocksKeys = new ArrayList<>(this.dialogueUsedBlocksMap.keySet());
		nbt.putInt("dialogueUsedBlocksKeysSize", dialogueUsedBlocksKeys.size());
		for (int i = 0; i < dialogueUsedBlocksKeys.size(); i++) {
			String key = dialogueUsedBlocksKeys.get(i);
			nbt.putString("dialogueUsedBlocks_key_" + i, key);
			nbt.putInt("dialogueUsedBlocks_entry_X_" + i, this.dialogueUsedBlocksMap.get(key).getX());
			nbt.putInt("dialogueUsedBlocks_entry_Y_" + i, this.dialogueUsedBlocksMap.get(key).getY());
			nbt.putInt("dialogueUsedBlocks_entry_Z_" + i, this.dialogueUsedBlocksMap.get(key).getZ());
		}

		List<String> dialogueTriggeredBlocksKeys = new ArrayList<>(this.dialogueTriggeredBlocksMap.keySet());
		nbt.putInt("dialogueTriggeredBlocksKeysSize", dialogueTriggeredBlocksKeys.size());
		for (int i = 0; i < dialogueTriggeredBlocksKeys.size(); i++) {
			String key = dialogueTriggeredBlocksKeys.get(i);
			nbt.putString("dialogueTriggeredBlocks_key_" + i, key);
			nbt.putInt("dialogueTriggeredBlocks_entry_X_" + i, this.dialogueTriggeredBlocksMap.get(key).getLeft().getX());
			nbt.putInt("dialogueTriggeredBlocks_entry_Y_" + i, this.dialogueTriggeredBlocksMap.get(key).getLeft().getY());
			nbt.putInt("dialogueTriggeredBlocks_entry_Z_" + i, this.dialogueTriggeredBlocksMap.get(key).getLeft().getZ());
			nbt.putBoolean("dialogueTriggeredBlocks_entry_resets_" + i, this.dialogueTriggeredBlocksMap.get(key).getRight());
		}

		nbt.putInt("startingDialogueListSize", this.startingDialogueList.size());
		for (int i = 0; i < this.startingDialogueList.size(); i++) {
			nbt.putString("startingDialogueList_name_" + i, this.startingDialogueList.get(i));
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.dialogueUsedBlocksMap.clear();
		int dialogueUsedBlocksKeysSize = nbt.getInt("dialogueUsedBlocksKeysSize");
		for (int i = 0; i < dialogueUsedBlocksKeysSize; i++) {
			this.dialogueUsedBlocksMap.put(nbt.getString("dialogueUsedBlocks_key_" + i), new BlockPos(
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_X_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Y_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Z_" + i), -48, 48)
			));
		}

		this.dialogueTriggeredBlocksMap.clear();
		int dialogueTriggeredBlocksKeysSize = nbt.getInt("dialogueTriggeredBlocksKeysSize");
		for (int i = 0; i < dialogueTriggeredBlocksKeysSize; i++) {
			this.dialogueTriggeredBlocksMap.put(nbt.getString("dialogueTriggeredBlocks_key_" + i), new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_X_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Y_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Z_" + i), -48, 48)
					), nbt.getBoolean("dialogueTriggeredBlocks_entry_resets_" + i)));
		}

		this.startingDialogueList.clear();
		int startingDialogueListSize = nbt.getInt("startingDialogueListSize");
		for (int i = 0; i < startingDialogueListSize; i++) {
			this.startingDialogueList.add(nbt.getString("startingDialogueList_name_" + i));
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

	@Nullable
	public static Dialogue getDialogue(PlayerEntity player, DialogueBlockEntity dialogueBlockEntity) {
		if (dialogueBlockEntity.startingDialogueList.isEmpty()) {
			return null;
		}
		PlayerAdvancementTracker playerAdvancementTracker = null;
		ServerAdvancementLoader serverAdvancementLoader = null;

		if (player instanceof ServerPlayerEntity serverPlayerEntity) {
			playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
			MinecraftServer minecraftServer = serverPlayerEntity.getServer();
			if (minecraftServer != null) {
				serverAdvancementLoader = minecraftServer.getAdvancementLoader();
			}
		}
		Identifier lockAdvancement;
		Identifier unlockAdvancement;

		for (String dialogueEntry : dialogueBlockEntity.startingDialogueList) {
			Dialogue dialogue = DialoguesRegistry.registeredDialogues.get(Identifier.tryParse(dialogueEntry));
			if (dialogue != null) {
				lockAdvancement = dialogue.lockAdvancement();
				unlockAdvancement = dialogue.unlockAdvancement();

				AdvancementEntry lockAdvancementEntry = null;
				AdvancementEntry unlockAdvancementEntry = null;
				if (serverAdvancementLoader != null) {
						lockAdvancementEntry = serverAdvancementLoader.get(lockAdvancement);
						unlockAdvancementEntry = serverAdvancementLoader.get(unlockAdvancement);
				}
				if (playerAdvancementTracker != null) {
					if (lockAdvancement == null || (lockAdvancementEntry != null && !playerAdvancementTracker.getProgress(lockAdvancementEntry).isDone()) && (unlockAdvancement == null || (unlockAdvancementEntry != null && playerAdvancementTracker.getProgress(unlockAdvancementEntry).isDone()))) {
						return dialogue;
					}
				}
			}
		}
		return null;
	}

	public HashMap<String, BlockPos> getDialogueUsedBlocksMap() {
		return this.dialogueUsedBlocksMap;
	}

	public void setDialogueUsedBlocksMap(HashMap<String, BlockPos> dialogueUsedBlocks) {
		this.dialogueUsedBlocksMap = dialogueUsedBlocks;
	}

	public HashMap<String, MutablePair<BlockPos, Boolean>> getDialogueTriggeredBlocksMap() {
		return this.dialogueTriggeredBlocksMap;
	}

	public void setDialogueTriggeredBlocksMap(HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocks) {
		this.dialogueTriggeredBlocksMap = dialogueTriggeredBlocks;
	}

	public List<String> getStartingDialogueList() {
		return this.startingDialogueList;
	}

	public void setStartingDialogueList(List<String> startingDialogueList) {
		this.startingDialogueList = startingDialogueList;
	}

	@Override
	public List<MutablePair<String, BlockPos>> getDialogueUsedBlocks() {
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = new ArrayList<>();
		List<String> dialogueUsedBlocksKeys = new ArrayList<>(this.dialogueUsedBlocksMap.keySet());
		for (String key : dialogueUsedBlocksKeys) {
			dialogueUsedBlocks.add(new MutablePair<>(key, this.pos.add(this.dialogueUsedBlocksMap.get(key))));
		}
		return dialogueUsedBlocks;
	}

	@Override
	public List<MutablePair<String, MutablePair<BlockPos, Boolean>>> getDialogueTriggeredBlocks() {
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = new ArrayList<>();
		List<String> dialogueTriggeredBlocksKeys = new ArrayList<>(this.dialogueTriggeredBlocksMap.keySet());
		for (String key : dialogueTriggeredBlocksKeys) {
			dialogueTriggeredBlocks.add(new MutablePair<>(key, new MutablePair<>(this.pos.add(this.dialogueTriggeredBlocksMap.get(key).getLeft()), this.dialogueTriggeredBlocksMap.get(key).getRight())));
		}
		return dialogueTriggeredBlocks;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocksMap.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocksMap.compute(key, (k, oldBlockPos) -> BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos, blockRotation));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocksMap.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocksMap.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.dialogueTriggeredBlocksMap.put(key, oldBlockPos);
				}
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocksMap.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocksMap.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.FRONT_BACK));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocksMap.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocksMap.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
					this.dialogueTriggeredBlocksMap.put(key, oldBlockPos);
				}
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocksMap.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocksMap.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.LEFT_RIGHT));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocksMap.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocksMap.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
					this.dialogueTriggeredBlocksMap.put(key, oldBlockPos);
				}
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
