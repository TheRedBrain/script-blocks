package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateDialogueBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDialogueBlockPacket> {

	@Override
	public void receive(UpdateDialogueBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dialogueBlockPosition);
		BlockState blockState = world.getBlockState(dialogueBlockPosition);

		if (blockEntity instanceof DialogueBlockEntity dialogueBlockEntity) {

			dialogueBlockEntity.setDialogueUsedBlocksMap(dialogueUsedBlocksMap);
			dialogueBlockEntity.setDialogueTriggeredBlocksMap(dialogueTriggeredBlocksMap);
			dialogueBlockEntity.setStartingDialogueList(startingDialogueList);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dialogueBlockEntity.markDirty();
			world.updateListeners(dialogueBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}

	}
}
