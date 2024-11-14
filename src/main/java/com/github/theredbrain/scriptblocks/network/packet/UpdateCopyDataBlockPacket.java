package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateCopyDataBlockPacket(
		BlockPos copyDataBlockPosition,
		BlockPos firstDataProvidingBlockPositionOffset,
		BlockPos secondDataProvidingBlockPositionOffset,
		String dataIdentifier
) implements CustomPayload {
	public static final Id<UpdateCopyDataBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_copy_data_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateCopyDataBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateCopyDataBlockPacket::write, UpdateCopyDataBlockPacket::new);

	public UpdateCopyDataBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.copyDataBlockPosition);
		registryByteBuf.writeBlockPos(this.firstDataProvidingBlockPositionOffset);
		registryByteBuf.writeBlockPos(this.secondDataProvidingBlockPositionOffset);
		registryByteBuf.writeString(this.dataIdentifier);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
