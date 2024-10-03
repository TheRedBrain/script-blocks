package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Location;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record LocationsSyncPacket(Map<Identifier, Location> registeredLocations) implements CustomPayload {
	public static final Id<LocationsSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("locations_sync"));
	public static final PacketCodec<RegistryByteBuf, LocationsSyncPacket> PACKET_CODEC = PacketCodec.of(LocationsSyncPacket::write, LocationsSyncPacket::read);

	public static LocationsSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, Location> newLocations = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			Location location = registryByteBuf.decodeAsJson(Location.CODEC);
			newLocations.put(identifier, location);
		}
		return new LocationsSyncPacket(newLocations);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredLocations.size());
		for (var entry : registeredLocations.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(Location.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}