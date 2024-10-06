package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.network.packet.BossesSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswersSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.DialoguesSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.LocationsSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import com.github.theredbrain.scriptblocks.network.packet.ServerConfigSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.ShopsSyncPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(value = EnvType.CLIENT)
public class ClientPacketRegistry {

	public static void init() {

		ClientPlayNetworking.registerGlobalReceiver(BossesSyncPacket.PACKET_ID, (payload, context) -> {
			BossesRegistry.registeredBosses = payload.registeredBosses();
		});
		ClientPlayNetworking.registerGlobalReceiver(DialoguesSyncPacket.PACKET_ID, (payload, context) -> {
			DialoguesRegistry.registeredDialogues = payload.registeredDialogues();
		});
		ClientPlayNetworking.registerGlobalReceiver(DialogueAnswersSyncPacket.PACKET_ID, (payload, context) -> {
			DialogueAnswersRegistry.registeredDialogueAnswers = payload.registeredDialogueAnswers();
		});
		ClientPlayNetworking.registerGlobalReceiver(LocationsSyncPacket.PACKET_ID, (payload, context) -> {
			LocationsRegistry.registeredLocations = payload.registeredLocations();
		});
		ClientPlayNetworking.registerGlobalReceiver(ShopsSyncPacket.PACKET_ID, (payload, context) -> {
			ShopsRegistry.registeredShops = payload.registeredShops();
		});
		ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.PACKET_ID, (payload, context) -> {
			((DuckPlayerEntityMixin) context.player()).scriptblocks$sendAnnouncement(payload.announcement());
		});
		ClientPlayNetworking.registerGlobalReceiver(ServerConfigSyncPacket.PACKET_ID, (payload, context) -> {
			ScriptBlocks.serverConfig = payload.serverConfig();
		});
	}
}
