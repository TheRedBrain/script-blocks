package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateDataAccessBlockPacket(
		BlockPos dataAccessBlockPosition,
		BlockPos dataProvidingBlockPositionOffset,
		BlockPos firstTriggeredBlockPositionOffset,
		boolean firstTriggeredBlockResets,
		BlockPos secondTriggeredBlockPositionOffset,
		boolean secondTriggeredBlockResets,
		boolean isWriting,
		String dataIdentifier,
		int comparedDataValue,
		String dataReadingMode,
		boolean isAdding,
		int newDataValue
) implements CustomPayload {
	public static final Id<UpdateDataAccessBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_data_access_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDataAccessBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDataAccessBlockPacket::write, UpdateDataAccessBlockPacket::new);

	public UpdateDataAccessBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readString(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataAccessBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeBlockPos(this.firstTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.firstTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.secondTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.secondTriggeredBlockResets);
		registryByteBuf.writeBoolean(this.isWriting);
		registryByteBuf.writeString(this.dataIdentifier);
		registryByteBuf.writeInt(this.comparedDataValue);
		registryByteBuf.writeString(this.dataReadingMode);
		registryByteBuf.writeBoolean(this.isAdding);
		registryByteBuf.writeInt(this.newDataValue);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
