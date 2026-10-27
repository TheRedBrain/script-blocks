package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateShopBlockPacket(
		BlockPos shopBlockPosition,
		String shopIdentifier
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateShopBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_shop_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateShopBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateShopBlockPacket::write, UpdateShopBlockPacket::new);

	public UpdateShopBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.shopBlockPosition);
		registryByteBuf.writeUtf(this.shopIdentifier);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
