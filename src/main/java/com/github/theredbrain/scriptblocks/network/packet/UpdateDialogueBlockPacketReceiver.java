package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateDialogueBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDialogueBlockPacket> {

	@Override
	public void receive(UpdateDialogueBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos dialogueBlockPosition = payload.dialogueBlockPosition();

		List<MutablePair<String, BlockPos>> dialogueUsedBlocksList = payload.dialogueUsedBlocksList();
		HashMap<String, BlockPos> dialogueUsedBlocksMap = new HashMap<>();
		for (MutablePair<String, BlockPos> usedBlock : dialogueUsedBlocksList) {
			dialogueUsedBlocksMap.put(usedBlock.getLeft(), usedBlock.getRight());
		}

		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList = payload.dialogueTriggeredBlocksList();
		HashMap<String, MutablePair<BlockPos, Boolean>> dialogueTriggeredBlocksMap = new HashMap<>();
		for (MutablePair<String, MutablePair<BlockPos, Boolean>> triggeredBlock : dialogueTriggeredBlocksList) {
			dialogueTriggeredBlocksMap.put(triggeredBlock.getLeft(), triggeredBlock.getRight());
		}

		List<String> startingDialogueList = new ArrayList<>(payload.startingDialogueList());

		BlockPos dataBlockOffset = payload.dataBlockOffset();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(dialogueBlockPosition);
		BlockState blockState = serverLevel.getBlockState(dialogueBlockPosition);

		if (blockEntity instanceof DialogueBlockEntity dialogueBlockEntity) {

			dialogueBlockEntity.setUseRelayBlocks(dialogueUsedBlocksMap);
			dialogueBlockEntity.setTriggeredBlocks(dialogueTriggeredBlocksMap);
			dialogueBlockEntity.setStartingDialogueList(startingDialogueList);
			dialogueBlockEntity.setDataProvidingBlockPosOffset(dataBlockOffset);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			dialogueBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(dialogueBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}

	}
}
