package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record UpdateUUIDListRelayBlockPacket(
		BlockPos uuidListRelayBlockPosition,
		List<BlockPos> uuidListProviderPositionOffsets
) implements CustomPayload {
	public static final Id<UpdateUUIDListRelayBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_uuid_list_relay_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateUUIDListRelayBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateUUIDListRelayBlockPacket::write, UpdateUUIDListRelayBlockPacket::new);

	public UpdateUUIDListRelayBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(BlockPos.PACKET_CODEC)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.uuidListRelayBlockPosition);
		registryByteBuf.writeCollection(this.uuidListProviderPositionOffsets, BlockPos.PACKET_CODEC);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
