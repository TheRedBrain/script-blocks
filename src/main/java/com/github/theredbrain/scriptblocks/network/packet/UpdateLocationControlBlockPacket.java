package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateLocationControlBlockPacket(BlockPos locationControlBlockPosition,
											   BlockPos mainEntrancePositionOffset, double mainEntranceYaw,
											   double mainEntrancePitch,
											   List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> sideEntrancesList,
											   BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets,
											   boolean shouldAlwaysReset) implements CustomPayload {
	public static final CustomPayload.Id<UpdateLocationControlBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_location_control_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateLocationControlBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateLocationControlBlockPacket::write, UpdateLocationControlBlockPacket::new);

	public UpdateLocationControlBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.locationControlBlockPosition);
		registryByteBuf.writeBlockPos(this.mainEntrancePositionOffset);
		registryByteBuf.writeDouble(this.mainEntranceYaw);
		registryByteBuf.writeDouble(this.mainEntrancePitch);
		registryByteBuf.writeCollection(this.sideEntrancesList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBoolean(this.shouldAlwaysReset);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
