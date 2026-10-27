package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateDataWritingBlockPacket(
		BlockPos dataAccessBlockPosition,
		BlockPos dataProvidingBlockPositionOffset,
		String dataIdentifier,
		String newDataValue
) implements CustomPacketPayload {
	public static final Type<UpdateDataWritingBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_data_writing_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDataWritingBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDataWritingBlockPacket::write, UpdateDataWritingBlockPacket::new);

	public UpdateDataWritingBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataAccessBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeUtf(this.dataIdentifier);
		registryByteBuf.writeUtf(this.newDataValue);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
