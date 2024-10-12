package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTeleporterBlockPacket(
		BlockPos teleportBlockPosition,
		boolean showActivationArea,
		boolean showAdventureScreen,
		Vec3i activationAreaDimensions,
		BlockPos activationAreaPositionOffset,
		BlockPos accessPositionOffset,
		boolean setAccessPosition,
		List<String> statusEffectsToDecrementLevelOnTeleport,
		boolean onlyTeleportDimensionOwner,
		boolean teleportTeam,
		String teleportationMode,
		BlockPos directTeleportPositionOffset,
		double directTeleportOrientationYaw,
		double directTeleportOrientationPitch,
		String spawnPointType,
		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList,
		String teleporterName,
		String currentTargetIdentifierLabel,
		String currentTargetOwnerLabel,
		boolean showRegenerateButton,
		String teleportButtonLabel,
		String cancelTeleportButtonLabel
) implements CustomPayload {
	public static final CustomPayload.Id<UpdateTeleporterBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_teleporter_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTeleporterBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTeleporterBlockPacket::write, UpdateTeleporterBlockPacket::new);

	public UpdateTeleporterBlockPacket(RegistryByteBuf registryByteBuf) {
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
				registryByteBuf.readList(PacketCodecs.STRING),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readString(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_INTEGER),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teleportBlockPosition);

		registryByteBuf.writeBoolean(this.showActivationArea);

		registryByteBuf.writeBoolean(this.showAdventureScreen);

		registryByteBuf.writeInt(this.activationAreaDimensions.getX());
		registryByteBuf.writeInt(this.activationAreaDimensions.getY());
		registryByteBuf.writeInt(this.activationAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.activationAreaPositionOffset);

		registryByteBuf.writeBlockPos(this.accessPositionOffset);
		registryByteBuf.writeBoolean(this.setAccessPosition);

		registryByteBuf.writeCollection(this.statusEffectsToDecrementLevelOnTeleport, PacketCodecs.STRING);

		registryByteBuf.writeBoolean(this.onlyTeleportDimensionOwner);
		registryByteBuf.writeBoolean(this.teleportTeam);

		registryByteBuf.writeString(this.teleportationMode);

		registryByteBuf.writeBlockPos(this.directTeleportPositionOffset);
		registryByteBuf.writeDouble(this.directTeleportOrientationYaw);
		registryByteBuf.writeDouble(this.directTeleportOrientationPitch);

		registryByteBuf.writeString(this.spawnPointType);

		registryByteBuf.writeCollection(this.locationsList, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_INTEGER);

		registryByteBuf.writeString(this.teleporterName);
		registryByteBuf.writeString(this.currentTargetIdentifierLabel);
		registryByteBuf.writeString(this.currentTargetOwnerLabel);
		registryByteBuf.writeBoolean(this.showRegenerateButton);
		registryByteBuf.writeString(this.teleportButtonLabel);
		registryByteBuf.writeString(this.cancelTeleportButtonLabel);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
