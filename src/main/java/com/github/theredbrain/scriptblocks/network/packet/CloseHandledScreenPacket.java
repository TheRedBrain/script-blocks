package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public class CloseHandledScreenPacket implements FabricPacket {
    public static final PacketType<CloseHandledScreenPacket> TYPE = PacketType.create(
            ScriptBlocksMod.identifier("close_handled_screen"),
            CloseHandledScreenPacket::new
    );

    public CloseHandledScreenPacket() {
    }

    public CloseHandledScreenPacket(PacketByteBuf buf) {
        this();
    }
    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
    @Override
    public void write(PacketByteBuf buf) {
    }
}
