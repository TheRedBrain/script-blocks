package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DialogueAnswerPacket(
		Identifier answerIdentifier,
		BlockPos dataBlockPos,
		List<MutablePair<String, BlockPos>> dialogueUsedBlocks,
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<DialogueAnswerPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("dialogue_answer"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DialogueAnswerPacket> PACKET_CODEC = StreamCodec.ofMember(DialogueAnswerPacket::write, DialogueAnswerPacket::new);

	public DialogueAnswerPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readIdentifier(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.answerIdentifier);
		registryByteBuf.writeBlockPos(this.dataBlockPos);
		registryByteBuf.writeCollection(this.dialogueUsedBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
		registryByteBuf.writeCollection(this.dialogueTriggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
