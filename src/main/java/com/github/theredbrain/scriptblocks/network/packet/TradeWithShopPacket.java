package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TradeWithShopPacket(
		String shopIdentifier,
		int id
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<TradeWithShopPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("trade_with_shop"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TradeWithShopPacket> PACKET_CODEC = StreamCodec.ofMember(TradeWithShopPacket::write, TradeWithShopPacket::new);

	public TradeWithShopPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readUtf(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeUtf(this.shopIdentifier);
		registryByteBuf.writeInt(this.id);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
