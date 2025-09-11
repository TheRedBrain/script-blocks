package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class EventsRegistry {
	public static void initializeEvents() {

		// Config stage

		// Play stage

		PayloadTypeRegistry.playS2C().register(SendAnnouncementPacket.PACKET_ID, SendAnnouncementPacket.PACKET_CODEC);

	}
}
