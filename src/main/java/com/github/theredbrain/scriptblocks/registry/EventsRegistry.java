package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class EventsRegistry {
	public static void initializeEvents() {

		// Config stage

		// Play stage

		PayloadTypeRegistry.playS2C().register(SendAnnouncementPacket.PACKET_ID, SendAnnouncementPacket.PACKET_CODEC);

//		PayloadTypeRegistry.playS2C().register(ServerConfigSyncPacket.PACKET_ID, ServerConfigSyncPacket.PACKET_CODEC);
//		PayloadTypeRegistry.playS2C().register(BossesSyncPacket.PACKET_ID, BossesSyncPacket.PACKET_CODEC);
//		PayloadTypeRegistry.playS2C().register(DialoguesSyncPacket.PACKET_ID, DialoguesSyncPacket.PACKET_CODEC);
//		PayloadTypeRegistry.playS2C().register(DialogueAnswersSyncPacket.PACKET_ID, DialogueAnswersSyncPacket.PACKET_CODEC);
//		PayloadTypeRegistry.playS2C().register(LocationsSyncPacket.PACKET_ID, LocationsSyncPacket.PACKET_CODEC);
//		PayloadTypeRegistry.playS2C().register(ShopsSyncPacket.PACKET_ID, ShopsSyncPacket.PACKET_CODEC);
//		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
//			ServerPlayNetworking.send(handler.player, new ServerConfigSyncPacket(ScriptBlocks.SERVER_CONFIG));
//			ServerPlayNetworking.send(handler.player, new BossesSyncPacket(BossesRegistry.registeredBosses));
//			ServerPlayNetworking.send(handler.player, new DialoguesSyncPacket(DialoguesRegistry.registeredDialogues));
//			ServerPlayNetworking.send(handler.player, new DialogueAnswersSyncPacket(DialogueAnswersRegistry.registeredDialogueAnswers));
//			ServerPlayNetworking.send(handler.player, new LocationsSyncPacket(LocationsRegistry.registeredLocations));
//			ServerPlayNetworking.send(handler.player, new ShopsSyncPacket(ShopsRegistry.registeredShops));
//		});
//		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
//			for (ServerPlayerEntity player : PlayerLookup.all(server)) {
//				ServerPlayNetworking.send(player, new BossesSyncPacket(BossesRegistry.registeredBosses));
//				ServerPlayNetworking.send(player, new DialoguesSyncPacket(DialoguesRegistry.registeredDialogues));
//				ServerPlayNetworking.send(player, new DialogueAnswersSyncPacket(DialogueAnswersRegistry.registeredDialogueAnswers));
//				ServerPlayNetworking.send(player, new LocationsSyncPacket(LocationsRegistry.registeredLocations));
//				ServerPlayNetworking.send(player, new ShopsSyncPacket(ShopsRegistry.registeredShops));
//			}
//		});
	}
}
