package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record TeleportFromTeleporterBlockPacket(
		BlockPos teleportBlockPosition,
		String accessPositionDimension,
		String targetDimensionOwnerName,
		String targetLocation,
		String targetLocationEntrance,
		String dataId,
		String data
) implements CustomPayload {
	public static final CustomPayload.Id<TeleportFromTeleporterBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("teleport_from_teleporter_block"));
	public static final PacketCodec<RegistryByteBuf, TeleportFromTeleporterBlockPacket> PACKET_CODEC = PacketCodec.of(TeleportFromTeleporterBlockPacket::write, TeleportFromTeleporterBlockPacket::new);

	public TeleportFromTeleporterBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teleportBlockPosition);
		registryByteBuf.writeString(this.accessPositionDimension);
		registryByteBuf.writeString(this.targetDimensionOwnerName);
		registryByteBuf.writeString(this.targetLocation);
		registryByteBuf.writeString(this.targetLocationEntrance);
		registryByteBuf.writeString(this.dataId);
		registryByteBuf.writeString(this.data);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
