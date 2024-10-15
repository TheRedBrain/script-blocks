package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateDataRelayBlockPacket(
		BlockPos dataRelayBlockPosition,
		BlockPos dataProvidingBlockPositionOffset
) implements CustomPayload {
	public static final Id<UpdateDataRelayBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_data_relay_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDataRelayBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDataRelayBlockPacket::write, UpdateDataRelayBlockPacket::new);

	public UpdateDataRelayBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataRelayBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
