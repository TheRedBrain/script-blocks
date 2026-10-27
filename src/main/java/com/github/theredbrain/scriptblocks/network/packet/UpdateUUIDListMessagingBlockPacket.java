package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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
) implements CustomPacketPayload {
	public static final Type<UpdateUUIDListMessagingBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_uuid_list_messaging_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateUUIDListMessagingBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateUUIDListMessagingBlockPacket::write, UpdateUUIDListMessagingBlockPacket::new);

	public UpdateUUIDListMessagingBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.uuidListMessagingBlockPosition);
		registryByteBuf.writeUtf(this.messageMode);
		registryByteBuf.writeUtf(this.messageLocalisationString);
		registryByteBuf.writeUtf(this.subTitleLocalisationString);
		registryByteBuf.writeInt(this.titleFadeIn);
		registryByteBuf.writeInt(this.titleStay);
		registryByteBuf.writeInt(this.titleFadeOut);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeUtf(this.dataIdentifier);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
