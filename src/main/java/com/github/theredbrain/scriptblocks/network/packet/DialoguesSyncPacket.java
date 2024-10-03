package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record DialoguesSyncPacket(Map<Identifier, Dialogue> registeredDialogues) implements CustomPayload {
	public static final Id<DialoguesSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("dialogues_sync"));
	public static final PacketCodec<RegistryByteBuf, DialoguesSyncPacket> PACKET_CODEC = PacketCodec.of(DialoguesSyncPacket::write, DialoguesSyncPacket::read);

	public static DialoguesSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, Dialogue> newDialogues = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			Dialogue dialogue = registryByteBuf.decodeAsJson(Dialogue.CODEC);
			newDialogues.put(identifier, dialogue);
		}
		return new DialoguesSyncPacket(newDialogues);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredDialogues.size());
		for (var entry : registeredDialogues.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(Dialogue.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}