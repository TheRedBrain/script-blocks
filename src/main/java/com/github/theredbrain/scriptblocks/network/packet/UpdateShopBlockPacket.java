package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateShopBlockPacket(BlockPos shopBlockPosition, String shopIdentifier) implements CustomPayload {
	public static final CustomPayload.Id<UpdateShopBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_shop_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateShopBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateShopBlockPacket::write, UpdateShopBlockPacket::new);

	public UpdateShopBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.shopBlockPosition);
		registryByteBuf.writeString(this.shopIdentifier);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
