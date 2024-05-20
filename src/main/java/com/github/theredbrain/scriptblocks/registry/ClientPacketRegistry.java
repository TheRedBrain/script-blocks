package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.network.packet.CloseHandledScreenPacket;
import com.github.theredbrain.scriptblocks.network.packet.CloseHandledScreenPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacketReceiver;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class ClientPacketRegistry {

    public static void init() {

        ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.TYPE, new SendAnnouncementPacketReceiver());

        ClientPlayNetworking.registerGlobalReceiver(CloseHandledScreenPacket.TYPE, new CloseHandledScreenPacketReceiver());

        ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_DIALOGUES, (client, handler, buffer, responseSender) -> { // TODO convert to packet
            DialoguesRegistry.decodeRegistry(buffer);
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_DIALOGUE_ANSWERS, (client, handler, buffer, responseSender) -> { // TODO convert to packet
            DialogueAnswersRegistry.decodeRegistry(buffer);
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_LOCATIONS, (client, handler, buffer, responseSender) -> { // TODO convert to packet
            LocationsRegistry.decodeRegistry(buffer);
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_SHOPS, (client, handler, buffer, responseSender) -> { // TODO convert to packet
            ShopsRegistry.decodeRegistry(buffer);
        });
        ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.ServerConfigSync.ID, (client, handler, buf, responseSender) -> {
            ScriptBlocksMod.serverConfig = ServerPacketRegistry.ServerConfigSync.read(buf);
        });
    }
}
