package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record DialogueAnswersSyncPacket(Map<Identifier, DialogueAnswer> registeredDialogueAnswers) implements CustomPayload {
	public static final Id<DialogueAnswersSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("dialogue_answers_sync"));
	public static final PacketCodec<RegistryByteBuf, DialogueAnswersSyncPacket> PACKET_CODEC = PacketCodec.of(DialogueAnswersSyncPacket::write, DialogueAnswersSyncPacket::read);

	public static DialogueAnswersSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, DialogueAnswer> newDialogueAnswers = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			DialogueAnswer dialogueAnswer = registryByteBuf.decodeAsJson(DialogueAnswer.CODEC);
			newDialogueAnswers.put(identifier, dialogueAnswer);
		}
		return new DialogueAnswersSyncPacket(newDialogueAnswers);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredDialogueAnswers.size());
		for (var entry : registeredDialogueAnswers.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(DialogueAnswer.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}