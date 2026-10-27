package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTwoAreasPlayerDetectorBlockPacket(
		BlockPos twoAreasPlayerDetectorBlockPosition,
		boolean showFirstArea,
		boolean showSecondArea,
		Vec3i firstAreaDimensions,
		BlockPos firstAreaPositionOffset,
		Vec3i secondAreaDimensions,
		BlockPos secondAreaPositionOffset,
		BlockPos onEnteringFirstAreaTriggeredBlockPositionOffset,
		boolean onEnteringFirstAreaTriggeredBlockResets,
		BlockPos onEnteringSecondAreaTriggeredBlockPositionOffset,
		boolean onEnteringSecondAreaTriggeredBlockResets,
		BlockPos onEnteringFirstAreaUUIDListHandler,
		BlockPos onEnteringSecondAreaUUIDListHandler
) implements CustomPacketPayload {
	public static final Type<UpdateTwoAreasPlayerDetectorBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_two_areas_player_detector_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTwoAreasPlayerDetectorBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTwoAreasPlayerDetectorBlockPacket::write, UpdateTwoAreasPlayerDetectorBlockPacket::new);

	public UpdateTwoAreasPlayerDetectorBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.twoAreasPlayerDetectorBlockPosition);
		registryByteBuf.writeBoolean(this.showFirstArea);
		registryByteBuf.writeBoolean(this.showSecondArea);
		registryByteBuf.writeInt(this.firstAreaDimensions.getX());
		registryByteBuf.writeInt(this.firstAreaDimensions.getY());
		registryByteBuf.writeInt(this.firstAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.firstAreaPositionOffset);
		registryByteBuf.writeInt(this.secondAreaDimensions.getX());
		registryByteBuf.writeInt(this.secondAreaDimensions.getY());
		registryByteBuf.writeInt(this.secondAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.secondAreaPositionOffset);
		registryByteBuf.writeBlockPos(this.onEnteringFirstAreaTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.onEnteringFirstAreaTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onEnteringSecondAreaTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.onEnteringSecondAreaTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onEnteringFirstAreaUUIDListHandler);
		registryByteBuf.writeBlockPos(this.onEnteringSecondAreaUUIDListHandler);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
