package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record DialogueAnswerPacket(Identifier answerIdentifier, List<MutablePair<String, BlockPos>> dialogueUsedBlocks,
								   List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks) implements CustomPayload {
	public static final CustomPayload.Id<DialogueAnswerPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("dialogue_answer"));
	public static final PacketCodec<RegistryByteBuf, DialogueAnswerPacket> PACKET_CODEC = PacketCodec.of(DialogueAnswerPacket::write, DialogueAnswerPacket::new);

	public DialogueAnswerPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readIdentifier(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.answerIdentifier);
		registryByteBuf.writeCollection(this.dialogueUsedBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
		registryByteBuf.writeCollection(this.dialogueTriggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
