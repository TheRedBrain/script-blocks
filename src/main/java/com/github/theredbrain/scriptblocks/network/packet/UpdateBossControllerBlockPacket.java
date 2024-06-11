package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.util.PacketByteBufUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateBossControllerBlockPacket implements FabricPacket {
    public static final PacketType<UpdateBossControllerBlockPacket> TYPE = PacketType.create(
            ScriptBlocksMod.identifier("update_boss_controller_block"),
            UpdateBossControllerBlockPacket::new
    );

    public final BlockPos bossControllerBlockPosition;

    public final boolean showArea;
    public final Vec3i applicationAreaDimensions;
    public final BlockPos applicationAreaPositionOffset;

    public final String bossIdentifier;
    public final BlockPos entitySpawnPositionOffset;
    public final double entitySpawnOrientationPitch;
    public final double entitySpawnOrientationYaw;

    public final List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList;

    public UpdateBossControllerBlockPacket(BlockPos bossControllerBlockPosition, boolean showArea, Vec3i applicationAreaDimensions, BlockPos applicationAreaPositionOffset, String bossIdentifier, BlockPos entitySpawnPositionOffset, double entitySpawnOrientationPitch, double entitySpawnOrientationYaw, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList) {
        this.bossControllerBlockPosition = bossControllerBlockPosition;
        this.showArea = showArea;
        this.applicationAreaDimensions = applicationAreaDimensions;
        this.applicationAreaPositionOffset = applicationAreaPositionOffset;

        this.bossIdentifier = bossIdentifier;
        this.entitySpawnPositionOffset = entitySpawnPositionOffset;
        this.entitySpawnOrientationPitch = entitySpawnOrientationPitch;
        this.entitySpawnOrientationYaw = entitySpawnOrientationYaw;

        this.bossTriggeredBlocksList = bossTriggeredBlocksList;
    }

    public UpdateBossControllerBlockPacket(PacketByteBuf buf) {
        this(
                buf.readBlockPos(),
                buf.readBoolean(),
                new Vec3i(
                        buf.readInt(),
                        buf.readInt(),
                        buf.readInt()
                ),
                buf.readBlockPos(),
                buf.readString(),
                buf.readBlockPos(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readList(new PacketByteBufUtils.MutablePairStringMutablePairBlockPosBooleanReader())
        );
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.bossControllerBlockPosition);

        buf.writeBoolean(this.showArea);
        buf.writeInt(this.applicationAreaDimensions.getX());
        buf.writeInt(this.applicationAreaDimensions.getY());
        buf.writeInt(this.applicationAreaDimensions.getZ());
        buf.writeBlockPos(this.applicationAreaPositionOffset);

        buf.writeString(this.bossIdentifier);
        buf.writeBlockPos(this.entitySpawnPositionOffset);
        buf.writeDouble(this.entitySpawnOrientationPitch);
        buf.writeDouble(this.entitySpawnOrientationYaw);

        buf.writeCollection(this.bossTriggeredBlocksList, new PacketByteBufUtils.MutablePairStringMutablePairBlockPosBooleanWriter());
    }

}
