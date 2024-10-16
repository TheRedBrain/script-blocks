package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record TeleportFromTeleporterBlockPacket(
		BlockPos teleportBlockPosition,
		String accessPositionDimension,
		BlockPos accessPositionOffset,
		boolean setAccessPosition,
		boolean teleportTeam,
		String teleportationMode,
		BlockPos directTeleportPositionOffset,
		double directTeleportOrientationYaw,
		double directTeleportOrientationPitch,
		String spawnPointType,
		String targetDimensionOwnerName,
		String targetLocation,
		String targetLocationEntrance,
		List<String> statusEffectsToDecrementLevelOnTeleport,
		String dataId,
		int data
) implements CustomPayload {
	public static final CustomPayload.Id<TeleportFromTeleporterBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("teleport_from_teleporter_block"));
	public static final PacketCodec<RegistryByteBuf, TeleportFromTeleporterBlockPacket> PACKET_CODEC = PacketCodec.of(TeleportFromTeleporterBlockPacket::write, TeleportFromTeleporterBlockPacket::new);

	public TeleportFromTeleporterBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readList(PacketCodecs.STRING),
				registryByteBuf.readString(),
				registryByteBuf.readInt());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teleportBlockPosition);
		registryByteBuf.writeString(this.accessPositionDimension);
		registryByteBuf.writeBlockPos(this.accessPositionOffset);
		registryByteBuf.writeBoolean(this.setAccessPosition);
		registryByteBuf.writeBoolean(this.teleportTeam);
		registryByteBuf.writeString(this.teleportationMode);
		registryByteBuf.writeBlockPos(this.directTeleportPositionOffset);
		registryByteBuf.writeDouble(this.directTeleportOrientationYaw);
		registryByteBuf.writeDouble(this.directTeleportOrientationPitch);
		registryByteBuf.writeString(this.spawnPointType);
		registryByteBuf.writeString(this.targetDimensionOwnerName);
		registryByteBuf.writeString(this.targetLocation);
		registryByteBuf.writeString(this.targetLocationEntrance);
		registryByteBuf.writeCollection(this.statusEffectsToDecrementLevelOnTeleport, PacketCodecs.STRING);
		registryByteBuf.writeString(this.dataId);
		registryByteBuf.writeInt(this.data);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
