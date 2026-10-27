package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateUseRelayBlockPacket(
		BlockPos useRelayBlockPosition,
		BlockPos relayBlockPositionOffset
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateUseRelayBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_use_relay_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateUseRelayBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateUseRelayBlockPacket::write, UpdateUseRelayBlockPacket::new);

	public UpdateUseRelayBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.useRelayBlockPosition);
		registryByteBuf.writeBlockPos(this.relayBlockPositionOffset);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
