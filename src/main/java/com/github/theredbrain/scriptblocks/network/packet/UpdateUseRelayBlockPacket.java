package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateUseRelayBlockPacket(BlockPos useRelayBlockPosition,
										BlockPos relayBlockPositionOffset) implements CustomPayload {
	public static final CustomPayload.Id<UpdateUseRelayBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_use_relay_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateUseRelayBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateUseRelayBlockPacket::write, UpdateUseRelayBlockPacket::new);

	public UpdateUseRelayBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.useRelayBlockPosition);
		registryByteBuf.writeBlockPos(this.relayBlockPositionOffset);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
