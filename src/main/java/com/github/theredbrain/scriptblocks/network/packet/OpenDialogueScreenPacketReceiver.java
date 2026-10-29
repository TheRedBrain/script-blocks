package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.DialogueAnchor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class OpenDialogueScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<OpenDialogueScreenPacket> {
	@Override
	public void receive(OpenDialogueScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		String dialogueIdentifierString = payload.dialogueIdentifierString();
		BlockPos dataBlockPos = payload.dataBlockPos();
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks = payload.dialogueUsedBlocks();
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks = payload.dialogueTriggeredBlocks();

		if (!dialogueIdentifierString.isEmpty()) {
			DialogueAnchor.openDialogueScreen(serverPlayerEntity.level(), serverPlayerEntity.level().getServer(), serverPlayerEntity, dialogueIdentifierString, dataBlockPos, dialogueUsedBlocks, dialogueTriggeredBlocks);
		}
	}
}
