package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdatePlayerDetectorBlockPacket(
		BlockPos areaBlockPosition,
		boolean enableTicking,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		BlockPos onEnteringTriggeredBlockPositionOffset,
		boolean onEnteringTriggeredBlockResets,
		BlockPos onLeavingTriggeredBlockPositionOffset,
		boolean onLeavingTriggeredBlockResets,
		BlockPos onTriggeringTriggeredBlockPositionOffset,
		boolean onTriggeringTriggeredBlockResets,
		BlockPos onEnteringUUIDListHandler,
		BlockPos onLeavingUUIDListHandler,
		BlockPos onTriggeringUUIDListHandler
) implements CustomPacketPayload {
	public static final Type<UpdatePlayerDetectorBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_player_detector_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePlayerDetectorBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdatePlayerDetectorBlockPacket::write, UpdatePlayerDetectorBlockPacket::new);

	public UpdatePlayerDetectorBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
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
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.areaBlockPosition);
		registryByteBuf.writeBoolean(this.enableTicking);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeBlockPos(this.onEnteringTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.onEnteringTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onLeavingTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.onLeavingTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onTriggeringTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.onTriggeringTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onEnteringUUIDListHandler);
		registryByteBuf.writeBlockPos(this.onLeavingUUIDListHandler);
		registryByteBuf.writeBlockPos(this.onTriggeringUUIDListHandler);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
