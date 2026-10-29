package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateLocationControlBlockPacket(
		BlockPos locationControlBlockPosition,
		LevelData.RespawnData mainEntrance,
		List<MutablePair<String, LevelData.RespawnData>> sideEntrancesList,
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
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_RESPAWN_DATA),
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
		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.mainEntrance);
		registryByteBuf.writeCollection(this.sideEntrancesList, CustomPacketCodecs.MUTABLE_PAIR_STRING_RESPAWN_DATA);
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
