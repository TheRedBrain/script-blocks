package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetManualResetLocationControlBlockPacket(BlockPos locationControlBlockPosition,
													   boolean manualReset) implements CustomPayload {
	public static final CustomPayload.Id<SetManualResetLocationControlBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("set_manual_reset_location_control_block"));
	public static final PacketCodec<RegistryByteBuf, SetManualResetLocationControlBlockPacket> PACKET_CODEC = PacketCodec.of(SetManualResetLocationControlBlockPacket::write, SetManualResetLocationControlBlockPacket::new);

	public SetManualResetLocationControlBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBoolean());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.locationControlBlockPosition);
		registryByteBuf.writeBoolean(this.manualReset);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
