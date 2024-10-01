package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.DialogueBlock;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class OpenDialogueScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<OpenDialogueScreenPacket> {

	@Override
	public void receive(OpenDialogueScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		String responseDialogueIdentifier = payload.responseDialogueIdentifier();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(payload.dialogueBlockPos());

		if (blockEntity instanceof DialogueBlockEntity dialogueBlockEntity) {
			serverPlayerEntity.openHandledScreen(DialogueBlock.createDialogueBlockScreenHandlerFactory(dialogueBlockEntity.getCachedState(), world, dialogueBlockEntity.getPos(), responseDialogueIdentifier));
		}
	}
}
