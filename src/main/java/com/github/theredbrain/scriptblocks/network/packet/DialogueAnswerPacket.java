package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record DialogueAnswerPacket(BlockPos dialogueBlockPos, Identifier answerIdentifier) implements CustomPayload {
	public static final CustomPayload.Id<DialogueAnswerPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("dialogue_answer"));
	public static final PacketCodec<RegistryByteBuf, DialogueAnswerPacket> PACKET_CODEC = PacketCodec.of(DialogueAnswerPacket::write, DialogueAnswerPacket::new);

	public DialogueAnswerPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readIdentifier());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dialogueBlockPos);
		registryByteBuf.writeIdentifier(this.answerIdentifier);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
