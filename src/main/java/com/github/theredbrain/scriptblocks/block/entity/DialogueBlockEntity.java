package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacket;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DialogueBlockEntity extends RotatedBlockEntity implements DialogueAnchor {

	private HashMap<String, BlockPos> dialogueUsedBlocks = new HashMap<>();
	private HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocks = new HashMap<>();

	// TODO convert to a map?
	private List<MutablePair<String, MutablePair<String, String>>> startingDialogueList = new ArrayList<>();

	public DialogueBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DIALOGUE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		List<String> dialogueUsedBlocksKeys = new ArrayList<>(this.dialogueUsedBlocks.keySet());
		nbt.putInt("dialogueUsedBlocksKeysSize", dialogueUsedBlocksKeys.size());
		for (int i = 0; i < dialogueUsedBlocksKeys.size(); i++) {
			String key = dialogueUsedBlocksKeys.get(i);
			nbt.putString("dialogueUsedBlocks_key_" + i, key);
			nbt.putInt("dialogueUsedBlocks_entry_X_" + i, this.dialogueUsedBlocks.get(key).getX());
			nbt.putInt("dialogueUsedBlocks_entry_Y_" + i, this.dialogueUsedBlocks.get(key).getY());
			nbt.putInt("dialogueUsedBlocks_entry_Z_" + i, this.dialogueUsedBlocks.get(key).getZ());
		}

		List<String> dialogueTriggeredBlocksKeys = new ArrayList<>(this.dialogueTriggeredBlocks.keySet());
		nbt.putInt("dialogueTriggeredBlocksKeysSize", dialogueTriggeredBlocksKeys.size());
		for (int i = 0; i < dialogueTriggeredBlocksKeys.size(); i++) {
			String key = dialogueTriggeredBlocksKeys.get(i);
			nbt.putString("dialogueTriggeredBlocks_key_" + i, key);
			nbt.putInt("dialogueTriggeredBlocks_entry_X_" + i, this.dialogueTriggeredBlocks.get(key).getLeft().getX());
			nbt.putInt("dialogueTriggeredBlocks_entry_Y_" + i, this.dialogueTriggeredBlocks.get(key).getLeft().getY());
			nbt.putInt("dialogueTriggeredBlocks_entry_Z_" + i, this.dialogueTriggeredBlocks.get(key).getLeft().getZ());
			nbt.putBoolean("dialogueTriggeredBlocks_entry_resets_" + i, this.dialogueTriggeredBlocks.get(key).getRight());
		}

		nbt.putInt("startingDialogueListSize", this.startingDialogueList.size());
		for (int i = 0; i < this.startingDialogueList.size(); i++) {
			nbt.putString("startingDialogueList_name_" + i, this.startingDialogueList.get(i).getLeft());
			nbt.putString("startingDialogueList_lockAdvancement_" + i, this.startingDialogueList.get(i).getRight().getLeft());
			nbt.putString("startingDialogueList_unlockAdvancement_" + i, this.startingDialogueList.get(i).getRight().getRight());
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		this.dialogueUsedBlocks.clear();
		int dialogueUsedBlocksKeysSize = nbt.getInt("dialogueUsedBlocksKeysSize");
		for (int i = 0; i < dialogueUsedBlocksKeysSize; i++) {
			this.dialogueUsedBlocks.put(nbt.getString("dialogueUsedBlocks_key_" + i), new BlockPos(
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_X_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Y_" + i), -48, 48),
					MathHelper.clamp(nbt.getInt("dialogueUsedBlocks_entry_Z_" + i), -48, 48)
			));
		}

		this.dialogueTriggeredBlocks.clear();
		int dialogueTriggeredBlocksKeysSize = nbt.getInt("dialogueTriggeredBlocksKeysSize");
		for (int i = 0; i < dialogueTriggeredBlocksKeysSize; i++) {
			this.dialogueTriggeredBlocks.put(nbt.getString("dialogueTriggeredBlocks_key_" + i), new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_X_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Y_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("dialogueTriggeredBlocks_entry_Z_" + i), -48, 48)
					), nbt.getBoolean("dialogueTriggeredBlocks_entry_resets_" + i)));
		}

		this.startingDialogueList.clear();
		int startingDialogueListSize = nbt.getInt("startingDialogueListSize");
		for (int i = 0; i < startingDialogueListSize; i++) {
			this.startingDialogueList.add(
					new MutablePair<>(
							nbt.getString("startingDialogueList_name_" + i),
							new MutablePair<>(
									nbt.getString("startingDialogueList_lockAdvancement_" + i),
									nbt.getString("startingDialogueList_unlockAdvancement_" + i)
							)
					)
			);
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

	public static String getStartingDialogue(PlayerEntity player, DialogueBlockEntity dialogueBlockEntity) {
		if (dialogueBlockEntity.startingDialogueList.isEmpty()) {
			return "";
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
		String lockAdvancementString;
		String unlockAdvancementString;

		for (MutablePair<String, MutablePair<String, String>> dialogueEntry : dialogueBlockEntity.startingDialogueList) {
			lockAdvancementString = dialogueEntry.getRight().getLeft();
			unlockAdvancementString = dialogueEntry.getRight().getRight();

            AdvancementEntry lockAdvancementEntry = null;
            AdvancementEntry unlockAdvancementEntry = null;
			if (serverAdvancementLoader != null) {
				if (!lockAdvancementString.isEmpty()) {
					lockAdvancementEntry = serverAdvancementLoader.get(Identifier.tryParse(lockAdvancementString));
				}
				if (!unlockAdvancementString.isEmpty()) {
					unlockAdvancementEntry = serverAdvancementLoader.get(Identifier.tryParse(unlockAdvancementString));
				}
			}
			if (playerAdvancementTracker != null) {
				if (lockAdvancementString.isEmpty() || (lockAdvancementEntry != null && !playerAdvancementTracker.getProgress(lockAdvancementEntry).isDone()) && (unlockAdvancementString.isEmpty() || (unlockAdvancementEntry != null && playerAdvancementTracker.getProgress(unlockAdvancementEntry).isDone()))) {
					return dialogueEntry.getLeft();
				}
			}
		}
		return "";
	}

	public static void answer(PlayerEntity playerEntity, Identifier answerIdentifier, DialogueBlockEntity dialogueBlockEntity) {
		if (dialogueBlockEntity.getWorld() instanceof ServerWorld serverWorld) {
			ScriptBlocks.info("answer on server side");
			ServerPlayerEntity serverPlayerEntity = serverWorld.getServer().getPlayerManager().getPlayer(playerEntity.getGameProfile().getId());
			if (serverPlayerEntity != null) {
				ServerPlayNetworking.send(serverPlayerEntity, new DialogueAnswerPacket(
						dialogueBlockEntity.getPos(),
						answerIdentifier
				));
			}
		} else {
			ScriptBlocks.info("answer on client side");
		}
	}

	@Override
	public HashMap<String, BlockPos> getDialogueUsedBlocks() {
		return this.dialogueUsedBlocks;
	}

	public boolean setDialogueUsedBlocks(HashMap<String, BlockPos> dialogueUsedBlocks) {
		this.dialogueUsedBlocks = dialogueUsedBlocks;
		return true;
	}

	@Override
	public HashMap<String, MutablePair<BlockPos, Boolean>> getDialogueTriggeredBlocks() {
		return this.dialogueTriggeredBlocks;
	}

	// TODO check if input is valid
	public boolean setDialogueTriggeredBlocks(HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocks) {
		this.dialogueTriggeredBlocks = dialogueTriggeredBlocks;
		return true;
	}

	public List<MutablePair<String, MutablePair<String, String>>> getStartingDialogueList() {
		return this.startingDialogueList;
	}

	// TODO check if input is valid
	public boolean setStartingDialogueList(List<MutablePair<String, MutablePair<String, String>>> startingDialogueList) {
		this.startingDialogueList = startingDialogueList;
		return true;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocks.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos, blockRotation));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.dialogueTriggeredBlocks.put(key, oldBlockPos);
				}
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocks.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.FRONT_BACK));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
					this.dialogueTriggeredBlocks.put(key, oldBlockPos);
				}
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				List<String> keys = new ArrayList<>(this.dialogueUsedBlocks.keySet());
				for (String key : keys) {
					this.dialogueUsedBlocks.compute(key, (k, oldBlockPos) -> BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos, BlockMirror.LEFT_RIGHT));
				}
				keys = new ArrayList<>(this.dialogueTriggeredBlocks.keySet());
				for (String key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.dialogueTriggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
					this.dialogueTriggeredBlocks.put(key, oldBlockPos);
				}
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
