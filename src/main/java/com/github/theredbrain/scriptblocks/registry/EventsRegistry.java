package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.network.packet.BossesSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswersSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.DialoguesSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.LocationsSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.ServerConfigSyncPacket;
import com.github.theredbrain.scriptblocks.network.packet.ShopsSyncPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventsRegistry {
	public static void initializeEvents() {
		PayloadTypeRegistry.playS2C().register(BossesSyncPacket.PACKET_ID, BossesSyncPacket.PACKET_CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayNetworking.send(handler.player, new ServerConfigSyncPacket(ScriptBlocks.serverConfig));
			ServerPlayNetworking.send(handler.player, new BossesSyncPacket(BossesRegistry.registeredBosses));
			ServerPlayNetworking.send(handler.player, new DialoguesSyncPacket(DialoguesRegistry.registeredDialogues));
			ServerPlayNetworking.send(handler.player, new DialogueAnswersSyncPacket(DialogueAnswersRegistry.registeredDialogueAnswers));
			ServerPlayNetworking.send(handler.player, new LocationsSyncPacket(LocationsRegistry.registeredLocations));
			ServerPlayNetworking.send(handler.player, new ShopsSyncPacket(ShopsRegistry.registeredShops));
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			for (ServerPlayerEntity player : PlayerLookup.all(server)) {
				ServerPlayNetworking.send(player, new BossesSyncPacket(BossesRegistry.registeredBosses));
				ServerPlayNetworking.send(player, new DialoguesSyncPacket(DialoguesRegistry.registeredDialogues));
				ServerPlayNetworking.send(player, new DialogueAnswersSyncPacket(DialogueAnswersRegistry.registeredDialogueAnswers));
				ServerPlayNetworking.send(player, new LocationsSyncPacket(LocationsRegistry.registeredLocations));
				ServerPlayNetworking.send(player, new ShopsSyncPacket(ShopsRegistry.registeredShops));
			}
		});
	}
}
