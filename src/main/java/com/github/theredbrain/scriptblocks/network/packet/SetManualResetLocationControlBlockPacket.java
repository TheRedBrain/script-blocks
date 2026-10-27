package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetManualResetLocationControlBlockPacket(
		String targetDimensionOwnerName,
		String targetLocation,
		String targetEntrance,
		boolean checkForResetCost,
		boolean manualReset
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SetManualResetLocationControlBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("set_manual_reset_location_control_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetManualResetLocationControlBlockPacket> PACKET_CODEC = StreamCodec.ofMember(SetManualResetLocationControlBlockPacket::write, SetManualResetLocationControlBlockPacket::new);

	public SetManualResetLocationControlBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeUtf(this.targetDimensionOwnerName);
		registryByteBuf.writeUtf(this.targetLocation);
		registryByteBuf.writeUtf(this.targetEntrance);
		registryByteBuf.writeBoolean(this.checkForResetCost);
		registryByteBuf.writeBoolean(this.manualReset);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
