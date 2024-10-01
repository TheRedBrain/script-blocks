package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload;

public record TradeWithShopPacket(String shopIdentifier, int id) implements CustomPayload {
	public static final CustomPayload.Id<TradeWithShopPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("trade_with_shop"));
	public static final PacketCodec<RegistryByteBuf, TradeWithShopPacket> PACKET_CODEC = PacketCodec.of(TradeWithShopPacket::write, TradeWithShopPacket::new);

	public TradeWithShopPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readString(), registryByteBuf.readInt());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeString(this.shopIdentifier);
		registryByteBuf.writeInt(this.id);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
