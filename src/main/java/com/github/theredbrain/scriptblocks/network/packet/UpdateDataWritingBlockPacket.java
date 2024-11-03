package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateDataWritingBlockPacket(
		BlockPos dataAccessBlockPosition,
		BlockPos dataProvidingBlockPositionOffset,
		String dataIdentifier,
		String newDataValue
) implements CustomPayload {
	public static final Id<UpdateDataWritingBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_data_writing_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDataWritingBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDataWritingBlockPacket::write, UpdateDataWritingBlockPacket::new);

	public UpdateDataWritingBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataAccessBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeString(this.dataIdentifier);
		registryByteBuf.writeString(this.newDataValue);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
