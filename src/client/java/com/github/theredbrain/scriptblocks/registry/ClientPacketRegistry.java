package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(value = EnvType.CLIENT)
public class ClientPacketRegistry {

	public static void init() {

		ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.TYPE, (packet, player, responseSender) -> {
			((DuckPlayerEntityMixin) player).scriptblocks$sendAnnouncement(packet.announcement);
		});
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
		ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_SHOPS, (client, handler, buffer, responseSender) -> { // TODO convert to packet
			BossesRegistry.decodeRegistry(buffer);
		});
		ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.ServerConfigSync.ID, (client, handler, buf, responseSender) -> {
			ScriptBlocksMod.serverConfig = ServerPacketRegistry.ServerConfigSync.read(buf);
		});
	}
}
