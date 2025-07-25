package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.network.packet.SendAnnouncementPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(value = EnvType.CLIENT)
public class ClientPacketRegistry {

	public static void init() {

//		ClientPlayNetworking.registerGlobalReceiver(OpenDialogueScreenPacket.PACKET_ID, (payload, context) -> {
//
//			Dialogue dialogue = null;
//			World world = context.player().getWorld();
//			if (world != null) {
//				Optional<RegistryEntry.Reference<Dialogue>> optionalDialogueReference = world.getRegistryManager().get(CustomDynamicRegistries.DIALOGUE_REGISTRY_KEY).getEntry(Identifier.of(payload.dialogueIdentifierString()));
//				if (optionalDialogueReference.isPresent()) {
//					dialogue = optionalDialogueReference.get().value();
//				}
//			}
//
//			if (dialogue != null) {
//				context.client().setScreen(new DialogueScreen(context.player().getWorld(), dialogue, payload.dialogueUsedBlocks(), payload.dialogueTriggeredBlocks()));
//			}
//		});
//		ClientPlayNetworking.registerGlobalReceiver(BossesSyncPacket.PACKET_ID, (payload, context) -> {
//			BossesRegistry.registeredBosses = payload.registeredBosses();
//		});
//		ClientPlayNetworking.registerGlobalReceiver(DialoguesSyncPacket.PACKET_ID, (payload, context) -> {
//			DialoguesRegistry.registeredDialogues = payload.registeredDialogues();
//		});
//		ClientPlayNetworking.registerGlobalReceiver(DialogueAnswersSyncPacket.PACKET_ID, (payload, context) -> {
//			DialogueAnswersRegistry.registeredDialogueAnswers = payload.registeredDialogueAnswers();
//		});
//		ClientPlayNetworking.registerGlobalReceiver(LocationsSyncPacket.PACKET_ID, (payload, context) -> {
//			LocationsRegistry.registeredLocations = payload.registeredLocations();
//		});
//		ClientPlayNetworking.registerGlobalReceiver(ShopsSyncPacket.PACKET_ID, (payload, context) -> {
//			ShopsRegistry.registeredShops = payload.registeredShops();
//		});
		ClientPlayNetworking.registerGlobalReceiver(SendAnnouncementPacket.PACKET_ID, (payload, context) -> {
			((DuckPlayerEntityMixin) context.player()).scriptblocks$sendAnnouncement(payload.announcement());
		});
//		ClientPlayNetworking.registerGlobalReceiver(ServerConfigSyncPacket.PACKET_ID, (payload, context) -> {
//			ScriptBlocks.SERVER_CONFIG = payload.serverConfig();
//		});
	}
}
