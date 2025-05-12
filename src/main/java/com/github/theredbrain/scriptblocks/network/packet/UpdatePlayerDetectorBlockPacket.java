package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdatePlayerDetectorBlockPacket(
		BlockPos areaBlockPosition,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets
) implements CustomPayload {
	public static final Id<UpdatePlayerDetectorBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_player_detector_block"));
	public static final PacketCodec<RegistryByteBuf, UpdatePlayerDetectorBlockPacket> PACKET_CODEC = PacketCodec.of(UpdatePlayerDetectorBlockPacket::write, UpdatePlayerDetectorBlockPacket::new);

	public UpdatePlayerDetectorBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.areaBlockPosition);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
