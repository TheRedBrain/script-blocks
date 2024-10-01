package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.PacketByteBuf;

public class EventsRegistry {
	private static PacketByteBuf serverConfigSerialized = PacketByteBufs.create();

	public static void initializeEvents() {
		serverConfigSerialized = ServerPacketRegistry.ServerConfigSync.write(ScriptBlocks.serverConfig);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPacket(ServerPacketRegistry.ServerConfigSync.ID, serverConfigSerialized); // TODO convert to packet
			sender.sendPacket(ServerPacketRegistry.SYNC_DIALOGUES, DialoguesRegistry.getEncodedRegistry()); // TODO convert to packet
			sender.sendPacket(ServerPacketRegistry.SYNC_DIALOGUE_ANSWERS, DialogueAnswersRegistry.getEncodedRegistry()); // TODO convert to packet
			sender.sendPacket(ServerPacketRegistry.SYNC_LOCATIONS, LocationsRegistry.getEncodedRegistry()); // TODO convert to packet
			sender.sendPacket(ServerPacketRegistry.SYNC_SHOPS, ShopsRegistry.getEncodedRegistry()); // TODO convert to packet
			sender.sendPacket(ServerPacketRegistry.SYNC_BOSSES, BossesRegistry.getEncodedRegistry()); // TODO convert to packet
		});
	}

//	@Environment(EnvType.CLIENT)
//	public static void initializeClientEvents() {
////        ClientTickEvents.START_CLIENT_TICK.register(client -> {
////            boolean bl = client.options.leftKey.isPressed() || client.options.backKey.isPressed() || client.options.rightKey.isPressed();
////            boolean arePlayerYawChangesDisabledByAttacking = ScriptBlocksMod.serverConfig.disable_player_yaw_changes_during_attacks && ((MinecraftClient_BetterCombat) client).isWeaponSwingInProgress();
////            if (client.options.attackKey.isPressed() && !bl &&
////                    client.player != null && client.options.getPerspective() != Perspective.FIRST_PERSON) {
////                if (!arePlayerYawChangesDisabledByAttacking) {
////                    client.player.setYaw(BetterAdventureModeClient.INSTANCE.cameraYaw);
////                }
////            }
////            if (client.options.pickItemKey.isPressed() && !bl &&
////                    client.player != null && client.options.getPerspective() != Perspective.FIRST_PERSON) {
////                if (!arePlayerYawChangesDisabledByAttacking) {
////                    client.player.setYaw(BetterAdventureModeClient.INSTANCE.cameraYaw);
////                }
////            }
////            if (client.options.useKey.isPressed() && !bl &&
////                    client.player != null && client.options.getPerspective() != Perspective.FIRST_PERSON) {
////                if (!arePlayerYawChangesDisabledByAttacking) {
////                    client.player.setYaw(BetterAdventureModeClient.INSTANCE.cameraYaw);
////                }
////            }
////        });
//		ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.TYPE, (packet, player, responseSender) -> {
//			((DuckPlayerEntityMixin) player).scriptblocks$sendAnnouncement(packet.announcement);
//		});
//	}
}
