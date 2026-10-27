package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateLocationControlBlockPacket(
		BlockPos locationControlBlockPosition,
		BlockPos mainEntrancePositionOffset,
		double mainEntranceYaw,
		double mainEntrancePitch,
		List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> sideEntrancesList,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos dataSavingBlockPosOffset,
		boolean shouldAlwaysReset,
		boolean manualReset,
		int resetAreaMinX,
		int resetAreaMinZ,
		int resetAreaMaxX,
		int resetAreaMaxZ
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateLocationControlBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_location_control_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateLocationControlBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateLocationControlBlockPacket::write, UpdateLocationControlBlockPacket::new);

	public UpdateLocationControlBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.locationControlBlockPosition);
		registryByteBuf.writeBlockPos(this.mainEntrancePositionOffset);
		registryByteBuf.writeDouble(this.mainEntranceYaw);
		registryByteBuf.writeDouble(this.mainEntrancePitch);
		registryByteBuf.writeCollection(this.sideEntrancesList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBlockPos(this.dataSavingBlockPosOffset);
		registryByteBuf.writeBoolean(this.shouldAlwaysReset);
		registryByteBuf.writeBoolean(this.manualReset);
		registryByteBuf.writeInt(this.resetAreaMinX);
		registryByteBuf.writeInt(this.resetAreaMinZ);
		registryByteBuf.writeInt(this.resetAreaMaxX);
		registryByteBuf.writeInt(this.resetAreaMaxZ);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
