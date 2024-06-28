package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.github.theredbrain.scriptblocks.network.packet.AddStatusEffectPacket;
import com.github.theredbrain.scriptblocks.network.packet.AddStatusEffectPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacket;
import com.github.theredbrain.scriptblocks.network.packet.DialogueAnswerPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.LeaveHouseFromHousingScreenPacket;
import com.github.theredbrain.scriptblocks.network.packet.LeaveHouseFromHousingScreenPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.OpenDialogueScreenPacket;
import com.github.theredbrain.scriptblocks.network.packet.OpenDialogueScreenPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.ResetHouseHousingBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.ResetHouseHousingBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacket;
import com.github.theredbrain.scriptblocks.network.packet.SetHousingBlockOwnerPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.SetManualResetLocationControlBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.SetManualResetLocationControlBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.TeleportFromTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.TeleportFromTeleporterBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.TeleportToTeamPacket;
import com.github.theredbrain.scriptblocks.network.packet.TeleportToTeamPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.TradeWithShopPacket;
import com.github.theredbrain.scriptblocks.network.packet.TradeWithShopPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDelayTriggerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDelayTriggerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDialogueBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDialogueBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateEntranceDelegationBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateEntranceDelegationBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockAdventurePacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockAdventurePacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockCreativePacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateHousingBlockCreativePacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveLootBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateInteractiveLootBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateJigsawPlacerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateJigsawPlacerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateLocationControlBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateLocationControlBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateMimicBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateMimicBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRedstoneTriggerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRedstoneTriggerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRelayTriggerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateRelayTriggerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateShopBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeleporterBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTeleporterBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredAdvancementCheckerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredAdvancementCheckerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredSpawnerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredSpawnerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayBlockPacketReceiver;
import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ServerPacketRegistry {

	//    public static final Identifier SWAPPED_HAND_ITEMS_PACKET = ScriptBlocksMod.identifier("swapped_hand_items");
//    public static final Identifier CANCEL_ATTACK_PACKET = ScriptBlocksMod.identifier("attack_stamina_cost");
//    public static final Identifier ADD_STATUS_EFFECT_PACKET = ScriptBlocksMod.identifier("add_status_effect");
//    public static final Identifier SHEATHED_WEAPONS_PACKET = ScriptBlocksMod.identifier("sheathed_weapons"); // TODO if weapon sheathing is not visible in multiplayer
//
////    public static final Identifier SYNC_PLAYER_HOUSES = BetterAdventureModeCore.identifier("sync_player_houses");
//    public static final Identifier SYNC_CRAFTING_RECIPES = ScriptBlocksMod.identifier("sync_crafting_recipes");
	public static final Identifier SYNC_DIALOGUES = ScriptBlocksMod.identifier("sync_dialogues");
	public static final Identifier SYNC_DIALOGUE_ANSWERS = ScriptBlocksMod.identifier("sync_dialogue_answers");
	public static final Identifier SYNC_LOCATIONS = ScriptBlocksMod.identifier("sync_locations");
	public static final Identifier SYNC_SHOPS = ScriptBlocksMod.identifier("sync_shops");
	public static final Identifier SYNC_BOSSES = ScriptBlocksMod.identifier("sync_bosses");
//    public static final Identifier SYNC_WEAPON_POSES = ScriptBlocksMod.identifier("sync_weapon_poses");

	public static void init() {
//        ServerPlayNetworking.registerGlobalReceiver(SwapHandItemsPacket.TYPE, new SwapHandItemsPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(SheatheWeaponsPacket.TYPE, new SheatheWeaponsPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(TwoHandMainWeaponPacket.TYPE, new TwoHandMainWeaponPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(ToggleNecklaceAbilityPacket.TYPE, new ToggleNecklaceAbilityPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(OpenBackpackScreenPacket.TYPE, new OpenBackpackScreenPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(AttackStaminaCostPacket.TYPE, new AttackStaminaCostPacketReceiver());
//
		ServerPlayNetworking.registerGlobalReceiver(AddStatusEffectPacket.TYPE, new AddStatusEffectPacketReceiver());


		ServerPlayNetworking.registerGlobalReceiver(UpdateHousingBlockAdventurePacket.TYPE, new UpdateHousingBlockAdventurePacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateHousingBlockCreativePacket.TYPE, new UpdateHousingBlockCreativePacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(SetHousingBlockOwnerPacket.TYPE, new SetHousingBlockOwnerPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(ResetHouseHousingBlockPacket.TYPE, new ResetHouseHousingBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateJigsawPlacerBlockPacket.TYPE, new UpdateJigsawPlacerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateRedstoneTriggerBlockPacket.TYPE, new UpdateRedstoneTriggerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateRelayTriggerBlockPacket.TYPE, new UpdateRelayTriggerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredCounterBlockPacket.TYPE, new UpdateTriggeredCounterBlockPacketReceiver());

//        ServerPlayNetworking.registerGlobalReceiver(UpdateResetTriggerBlockPacket.TYPE, new UpdateResetTriggerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateDelayTriggerBlockPacket.TYPE, new UpdateDelayTriggerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateInteractiveLootBlockPacket.TYPE, new UpdateInteractiveLootBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateUseRelayBlockPacket.TYPE, new UpdateUseRelayBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(LeaveHouseFromHousingScreenPacket.TYPE, new LeaveHouseFromHousingScreenPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateMimicBlockPacket.TYPE, new UpdateMimicBlockPacketReceiver());

//        ServerPlayNetworking.registerGlobalReceiver(RemoveMannequinPacket.TYPE, new RemoveMannequinPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(UpdateMannequinSettingsPacket.TYPE, new UpdateMannequinSettingsPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(UpdateMannequinEquipmentPacket.TYPE, new UpdateMannequinEquipmentPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(UpdateMannequinModelPartsPacket.TYPE, new UpdateMannequinModelPartsPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(ExportImportMannequinEquipmentPacket.TYPE, new ExportImportMannequinEquipmentPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateBossControllerBlockPacket.TYPE, new UpdateBossControllerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredSpawnerBlockPacket.TYPE, new UpdateTriggeredSpawnerBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateLocationControlBlockPacket.TYPE, new UpdateLocationControlBlockPacketReceiver());

//        ServerPlayNetworking.registerGlobalReceiver(CraftFromCraftingBenchPacket.TYPE, new CraftFromCraftingBenchPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(ToggleUseStashForCraftingPacket.TYPE, new ToggleUseStashForCraftingPacketReceiver());
//
//        ServerPlayNetworking.registerGlobalReceiver(UpdateCraftingBenchScreenHandlerPropertyPacket.TYPE, new UpdateCraftingBenchScreenHandlerPropertyPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateEntranceDelegationBlockPacket.TYPE, new UpdateEntranceDelegationBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateAreaBlockPacket.TYPE, new UpdateAreaBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredAdvancementCheckerBlockPacket.TYPE, new UpdateTriggeredAdvancementCheckerBlockPacketReceiver());

		// --- teleporter packets

		ServerPlayNetworking.registerGlobalReceiver(UpdateTeleporterBlockPacket.TYPE, new UpdateTeleporterBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(SetManualResetLocationControlBlockPacket.TYPE, new SetManualResetLocationControlBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(TeleportFromTeleporterBlockPacket.TYPE, new TeleportFromTeleporterBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(TeleportToTeamPacket.TYPE, new TeleportToTeamPacketReceiver());

		// --- shop packets

		ServerPlayNetworking.registerGlobalReceiver(UpdateShopBlockPacket.TYPE, new UpdateShopBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(TradeWithShopPacket.TYPE, new TradeWithShopPacketReceiver());

		// --- dialogue packets

		ServerPlayNetworking.registerGlobalReceiver(UpdateDialogueBlockPacket.TYPE, new UpdateDialogueBlockPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(DialogueAnswerPacket.TYPE, new DialogueAnswerPacketReceiver());

		ServerPlayNetworking.registerGlobalReceiver(OpenDialogueScreenPacket.TYPE, new OpenDialogueScreenPacketReceiver());
	}

	public static class ServerConfigSync {
		public static Identifier ID = ScriptBlocksMod.identifier("server_config_sync");

		public static PacketByteBuf write(ServerConfig serverConfig) {
			var gson = new Gson();
			var json = gson.toJson(serverConfig);
			var buffer = PacketByteBufs.create();
			buffer.writeString(json);
			return buffer;
		}

		public static ServerConfig read(PacketByteBuf buffer) {
			var gson = new Gson();
			var json = buffer.readString();
			return gson.fromJson(json, ServerConfig.class);
		}
	}
}
