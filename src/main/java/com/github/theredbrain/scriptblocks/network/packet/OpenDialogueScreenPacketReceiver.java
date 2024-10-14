package com.github.theredbrain.scriptblocks.network.packet;
//
//import com.github.theredbrain.scriptblocks.block.DialogueBlock;
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//import net.minecraft.server.network.ServerPlayerEntity;
//
//public class OpenDialogueScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<OpenDialogueScreenPacket> {
//
//	@Override
//	public void receive(OpenDialogueScreenPacket payload, ServerPlayNetworking.Context context) {
//
//		ServerPlayerEntity serverPlayerEntity = context.player();
//
//		String dialogueIdentifierString = payload.dialogueIdentifierString();
//
//		serverPlayerEntity.openHandledScreen(DialogueBlock.createDialogueScreenHandlerFactory(dialogueIdentifierString));
//	}
//}
