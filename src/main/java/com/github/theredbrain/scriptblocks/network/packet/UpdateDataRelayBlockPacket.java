package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateDataRelayBlockPacket(
		BlockPos dataRelayBlockPosition,
		List<BlockPos> dataProvidingBlockPosOffsetList
) implements CustomPacketPayload {
	public static final Type<UpdateDataRelayBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_data_relay_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDataRelayBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDataRelayBlockPacket::write, UpdateDataRelayBlockPacket::new);

	public UpdateDataRelayBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(BlockPos.STREAM_CODEC)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataRelayBlockPosition);
		registryByteBuf.writeCollection(this.dataProvidingBlockPosOffsetList, BlockPos.STREAM_CODEC);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
