package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record UpdateUUIDListRelayBlockPacket(
		BlockPos uuidListRelayBlockPosition,
		List<BlockPos> uuidListProviderPositionOffsets
) implements CustomPacketPayload {
	public static final Type<UpdateUUIDListRelayBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_uuid_list_relay_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateUUIDListRelayBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateUUIDListRelayBlockPacket::write, UpdateUUIDListRelayBlockPacket::new);

	public UpdateUUIDListRelayBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(BlockPos.STREAM_CODEC)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.uuidListRelayBlockPosition);
		registryByteBuf.writeCollection(this.uuidListProviderPositionOffsets, BlockPos.STREAM_CODEC);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
