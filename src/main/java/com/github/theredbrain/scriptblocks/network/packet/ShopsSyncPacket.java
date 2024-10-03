package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Shop;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record ShopsSyncPacket(Map<Identifier, Shop> registeredShops) implements CustomPayload {
	public static final Id<ShopsSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("shops_sync"));
	public static final PacketCodec<RegistryByteBuf, ShopsSyncPacket> PACKET_CODEC = PacketCodec.of(ShopsSyncPacket::write, ShopsSyncPacket::read);

	public static ShopsSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, Shop> newShops = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			Shop shop = registryByteBuf.decodeAsJson(Shop.CODEC);
			newShops.put(identifier, shop);
		}
		return new ShopsSyncPacket(newShops);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredShops.size());
		for (var entry : registeredShops.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(Shop.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}