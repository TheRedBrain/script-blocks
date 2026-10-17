package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateUUIDListMessagingBlockPacket(
		BlockPos uuidListMessagingBlockPosition,
		String messageMode,
		String messageLocalisationString,
		String subTitleLocalisationString,
		int titleFadeIn,
		int titleStay,
		int titleFadeOut,
		BlockPos dataProvidingBlockPosOffset,
		String dataIdentifier
) implements CustomPayload {
	public static final Id<UpdateUUIDListMessagingBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_uuid_list_messaging_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateUUIDListMessagingBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateUUIDListMessagingBlockPacket::write, UpdateUUIDListMessagingBlockPacket::new);

	public UpdateUUIDListMessagingBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.uuidListMessagingBlockPosition);
		registryByteBuf.writeString(this.messageMode);
		registryByteBuf.writeString(this.messageLocalisationString);
		registryByteBuf.writeString(this.subTitleLocalisationString);
		registryByteBuf.writeInt(this.titleFadeIn);
		registryByteBuf.writeInt(this.titleStay);
		registryByteBuf.writeInt(this.titleFadeOut);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeString(this.dataIdentifier);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
