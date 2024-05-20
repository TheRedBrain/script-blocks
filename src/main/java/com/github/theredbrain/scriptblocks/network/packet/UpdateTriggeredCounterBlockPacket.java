package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.util.PacketByteBufUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateTriggeredCounterBlockPacket implements FabricPacket {
    public static final PacketType<UpdateTriggeredCounterBlockPacket> TYPE = PacketType.create(
            ScriptBlocksMod.identifier("update_triggered_counter_block"),
            UpdateTriggeredCounterBlockPacket::new
    );

    public final BlockPos triggeredCounterBlockPosition;

    public final List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList;

    public UpdateTriggeredCounterBlockPacket(BlockPos triggeredCounterBlockPosition, List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList) {
        this.triggeredCounterBlockPosition = triggeredCounterBlockPosition;
        this.triggeredBlocksList = triggeredBlocksList;
    }

    public UpdateTriggeredCounterBlockPacket(PacketByteBuf buf) {
        this(buf.readBlockPos(), buf.readList(new PacketByteBufUtils.MutablePairIntegerMutablePairBlockPosBooleanReader()));
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockPos(this.triggeredCounterBlockPosition);
        buf.writeCollection(this.triggeredBlocksList, new PacketByteBufUtils.MutablePairIntegerMutablePairBlockPosBooleanWriter());
    }

}
