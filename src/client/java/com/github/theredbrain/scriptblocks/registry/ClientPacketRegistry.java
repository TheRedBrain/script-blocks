package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import com.github.theredbrain.scriptblocks.network.packet.ServerConfigSyncPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(value = EnvType.CLIENT)
public class ClientPacketRegistry {

	public static void init() {

		ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.PACKET_ID, (payload, context) -> {
			((DuckPlayerEntityMixin) context.player()).scriptblocks$sendAnnouncement(payload.announcement());
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
		ClientPlayNetworking.registerGlobalReceiver(ServerPacketRegistry.SYNC_BOSSES, (client, handler, buffer, responseSender) -> { // TODO convert to packet
			BossesRegistry.decodeRegistry(buffer);
		});
		ClientPlayNetworking.registerGlobalReceiver(ServerConfigSyncPacket.PACKET_ID, (payload, context) -> {
			ScriptBlocks.serverConfig = payload.serverConfig();
		});
	}
}
