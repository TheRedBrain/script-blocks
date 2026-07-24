package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateDataModificationBlockPacket(
		BlockPos dataAccessBlockPosition,
		BlockPos dataProvidingBlockPositionOffset,
		String dataModificationMode,
		int addedIntegerValue
) implements CustomPayload {
	public static final Id<UpdateDataModificationBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_data_modification_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDataModificationBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDataModificationBlockPacket::write, UpdateDataModificationBlockPacket::new);

	public UpdateDataModificationBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataAccessBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeString(this.dataModificationMode);
		registryByteBuf.writeInt(this.addedIntegerValue);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
