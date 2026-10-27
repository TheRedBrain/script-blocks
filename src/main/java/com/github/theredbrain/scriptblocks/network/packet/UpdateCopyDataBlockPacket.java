package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateCopyDataBlockPacket(
		BlockPos copyDataBlockPosition,
		BlockPos firstDataProvidingBlockPositionOffset,
		BlockPos secondDataProvidingBlockPositionOffset,
		String dataIdentifier
) implements CustomPacketPayload {
	public static final Type<UpdateCopyDataBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_copy_data_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCopyDataBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateCopyDataBlockPacket::write, UpdateCopyDataBlockPacket::new);

	public UpdateCopyDataBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.copyDataBlockPosition);
		registryByteBuf.writeBlockPos(this.firstDataProvidingBlockPositionOffset);
		registryByteBuf.writeBlockPos(this.secondDataProvidingBlockPositionOffset);
		registryByteBuf.writeUtf(this.dataIdentifier);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
