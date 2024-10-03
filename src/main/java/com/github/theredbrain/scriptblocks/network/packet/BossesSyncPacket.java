package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Boss;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record BossesSyncPacket(Map<Identifier, Boss> registeredBosses) implements CustomPayload {
	public static final Id<BossesSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("bosses_sync"));
	public static final PacketCodec<RegistryByteBuf, BossesSyncPacket> PACKET_CODEC = PacketCodec.of(BossesSyncPacket::write, BossesSyncPacket::read);

	public static BossesSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, Boss> newBosses = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			Boss boss = registryByteBuf.decodeAsJson(Boss.CODEC);
			newBosses.put(identifier, boss);
		}
		return new BossesSyncPacket(newBosses);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredBosses.size());
		for (var entry : registeredBosses.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(Boss.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}