package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;

public class SendAnnouncementPacketReceiver implements ClientPlayNetworking.PlayPacketHandler<SendAnnouncementPacket> {

    @Override
    public void receive(SendAnnouncementPacket packet, ClientPlayerEntity player, PacketSender responseSender) {
        ((DuckPlayerEntityMixin)player).scriptblocks$sendAnnouncement(packet.announcement);
    }
}
