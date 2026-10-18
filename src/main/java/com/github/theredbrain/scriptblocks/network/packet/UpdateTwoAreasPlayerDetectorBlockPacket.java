package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

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
) implements CustomPayload {
	public static final Id<UpdateTwoAreasPlayerDetectorBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_two_areas_player_detector_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTwoAreasPlayerDetectorBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTwoAreasPlayerDetectorBlockPacket::write, UpdateTwoAreasPlayerDetectorBlockPacket::new);

	public UpdateTwoAreasPlayerDetectorBlockPacket(RegistryByteBuf registryByteBuf) {
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

	private void write(RegistryByteBuf registryByteBuf) {
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
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
