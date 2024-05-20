package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.util.PacketByteBufUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class UpdateTeleporterBlockPacket implements FabricPacket {
    public static final PacketType<UpdateTeleporterBlockPacket> TYPE = PacketType.create(
            ScriptBlocksMod.identifier("update_teleporter_block"),
            UpdateTeleporterBlockPacket::new
    );

    public final BlockPos teleportBlockPosition;

    public final boolean showActivationArea;
    public final boolean showAdventureScreen;
    public final Vec3i activationAreaDimensions;
    public final BlockPos activationAreaPositionOffset;

    public final BlockPos accessPositionOffset;
    public final boolean setAccessPosition;

    public final boolean onlyTeleportDimensionOwner;
    public final boolean teleportTeam;

    public final TeleporterBlockEntity.TeleportationMode teleportationMode;

    public final BlockPos directTeleportPositionOffset;
    public final double directTeleportOrientationYaw;
    public final double directTeleportOrientationPitch;

    public final TeleporterBlockEntity.SpawnPointType spawnPointType;

    public final List<Pair<String, String>> locationsList;

    public final String teleporterName;
    public final String currentTargetIdentifierLabel;
    public final String currentTargetOwnerLabel;
    public final boolean showRegenerateButton;
    public final String teleportButtonLabel;
    public final String cancelTeleportButtonLabel;

    public UpdateTeleporterBlockPacket(BlockPos teleportBlockPosition, boolean showActivationArea, boolean showAdventureScreen, Vec3i activationAreaDimensions, BlockPos activationAreaPositionOffset, BlockPos accessPositionOffset, boolean setAccessPosition, boolean onlyTeleportDimensionOwner, boolean teleportTeam, String teleportationMode, BlockPos directTeleportPositionOffset, double directTeleportOrientationYaw, double directTeleportOrientationPitch, String locationType, List<Pair<String, String>> locationsList, String teleporterName, String currentTargetIdentifierLabel, String currentTargetOwnerLabel, boolean showRegenerateButton, String teleportButtonLabel, String cancelTeleportButtonLabel) {
        this.teleportBlockPosition = teleportBlockPosition;

        this.showActivationArea = showActivationArea;
        this.showAdventureScreen = showAdventureScreen;

        this.activationAreaDimensions = activationAreaDimensions;
        this.activationAreaPositionOffset = activationAreaPositionOffset;

        this.accessPositionOffset = accessPositionOffset;
        this.setAccessPosition = setAccessPosition;

        this.onlyTeleportDimensionOwner = onlyTeleportDimensionOwner;
        this.teleportTeam = teleportTeam;

        this.teleportationMode = TeleporterBlockEntity.TeleportationMode.byName(teleportationMode).orElseGet(() -> TeleporterBlockEntity.TeleportationMode.DIRECT);
        this.directTeleportPositionOffset = directTeleportPositionOffset;
        this.directTeleportOrientationYaw = directTeleportOrientationYaw;
        this.directTeleportOrientationPitch = directTeleportOrientationPitch;

        this.spawnPointType = TeleporterBlockEntity.SpawnPointType.byName(locationType).orElseGet(() -> TeleporterBlockEntity.SpawnPointType.WORLD_SPAWN);

        this.locationsList = locationsList;

        this.teleporterName = teleporterName;
        this.currentTargetIdentifierLabel = currentTargetIdentifierLabel;
        this.currentTargetOwnerLabel = currentTargetOwnerLabel;
        this.showRegenerateButton = showRegenerateButton;
        this.teleportButtonLabel = teleportButtonLabel;
        this.cancelTeleportButtonLabel = cancelTeleportButtonLabel;
    }

    public UpdateTeleporterBlockPacket(PacketByteBuf buf) {
        this(
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readBoolean(),
                new Vec3i(
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt()
                ),
                buf.readBlockPos(),
                buf.readBlockPos(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readString(),
                buf.readBlockPos(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readString(),
                buf.readList(new PacketByteBufUtils.MutablePairStringStringReader()),
                buf.readString(),
                buf.readString(),
                buf.readString(),
                buf.readBoolean(),
                buf.readString(),
                buf.readString()
        );
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.teleportBlockPosition);

        buf.writeBoolean(this.showActivationArea);

        buf.writeBoolean(this.showAdventureScreen);

        buf.writeInt(this.activationAreaDimensions.getX());
        buf.writeInt(this.activationAreaDimensions.getY());
        buf.writeInt(this.activationAreaDimensions.getZ());
        buf.writeBlockPos(this.activationAreaPositionOffset);

        buf.writeBlockPos(this.accessPositionOffset);
        buf.writeBoolean(this.setAccessPosition);

        buf.writeBoolean(this.onlyTeleportDimensionOwner);
        buf.writeBoolean(this.teleportTeam);

        buf.writeString(this.teleportationMode.asString());

        buf.writeBlockPos(this.directTeleportPositionOffset);
        buf.writeDouble(this.directTeleportOrientationYaw);
        buf.writeDouble(this.directTeleportOrientationPitch);

        buf.writeString(this.spawnPointType.asString());

        buf.writeCollection(this.locationsList, new PacketByteBufUtils.MutablePairStringStringWriter());

        buf.writeString(this.teleporterName);
        buf.writeString(this.currentTargetIdentifierLabel);
        buf.writeString(this.currentTargetOwnerLabel);
        buf.writeBoolean(this.showRegenerateButton);
        buf.writeString(this.teleportButtonLabel);
        buf.writeString(this.cancelTeleportButtonLabel);
    }
}
