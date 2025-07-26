package com.github.theredbrain.scriptblocks.registry;

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
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaFillerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateAreaFillerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateBossControllerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateCopyDataBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateCopyDataBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataRelayBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataRelayBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataSavingBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataSavingBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataWritingBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateDataWritingBlockPacketReceiver;
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
import com.github.theredbrain.scriptblocks.network.packet.UpdatePlayerDetectorBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdatePlayerDetectorBlockPacketReceiver;
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
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredBeaconBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredBeaconBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredCounterBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDisplayBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredDisplayBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredEntityRemoverBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredEntityRemoverBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredRNGBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredRNGBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredSpawnerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredSpawnerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredVillagerSpawnerBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateTriggeredVillagerSpawnerBlockPacketReceiver;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayBlockPacket;
import com.github.theredbrain.scriptblocks.network.packet.UpdateUseRelayBlockPacketReceiver;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ServerPacketRegistry {

	public static void init() {

//		PayloadTypeRegistry.configurationS2C().register(Packets.SpellRegistrySync.PACKET_ID, Packets.SpellRegistrySync.CODEC);

		PayloadTypeRegistry.playC2S().register(UpdateDataWritingBlockPacket.PACKET_ID, UpdateDataWritingBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateDataWritingBlockPacket.PACKET_ID, new UpdateDataWritingBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateCopyDataBlockPacket.PACKET_ID, UpdateCopyDataBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateCopyDataBlockPacket.PACKET_ID, new UpdateCopyDataBlockPacketReceiver());

//		PayloadTypeRegistry.playC2S().register(UpdateDataAccessBlockPacket.PACKET_ID, UpdateDataAccessBlockPacket.PACKET_CODEC);
//		ServerPlayNetworking.registerGlobalReceiver(UpdateDataAccessBlockPacket.PACKET_ID, new UpdateDataAccessBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateDataRelayBlockPacket.PACKET_ID, UpdateDataRelayBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateDataRelayBlockPacket.PACKET_ID, new UpdateDataRelayBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateDataSavingBlockPacket.PACKET_ID, UpdateDataSavingBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateDataSavingBlockPacket.PACKET_ID, new UpdateDataSavingBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(AddStatusEffectPacket.PACKET_ID, AddStatusEffectPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(AddStatusEffectPacket.PACKET_ID, new AddStatusEffectPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateHousingBlockAdventurePacket.PACKET_ID, UpdateHousingBlockAdventurePacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateHousingBlockAdventurePacket.PACKET_ID, new UpdateHousingBlockAdventurePacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateHousingBlockCreativePacket.PACKET_ID, UpdateHousingBlockCreativePacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateHousingBlockCreativePacket.PACKET_ID, new UpdateHousingBlockCreativePacketReceiver());

		PayloadTypeRegistry.playC2S().register(SetHousingBlockOwnerPacket.PACKET_ID, SetHousingBlockOwnerPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetHousingBlockOwnerPacket.PACKET_ID, new SetHousingBlockOwnerPacketReceiver());

		PayloadTypeRegistry.playC2S().register(ResetHouseHousingBlockPacket.PACKET_ID, ResetHouseHousingBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ResetHouseHousingBlockPacket.PACKET_ID, new ResetHouseHousingBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateJigsawPlacerBlockPacket.PACKET_ID, UpdateJigsawPlacerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateJigsawPlacerBlockPacket.PACKET_ID, new UpdateJigsawPlacerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateRedstoneTriggerBlockPacket.PACKET_ID, UpdateRedstoneTriggerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateRedstoneTriggerBlockPacket.PACKET_ID, new UpdateRedstoneTriggerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateRelayTriggerBlockPacket.PACKET_ID, UpdateRelayTriggerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateRelayTriggerBlockPacket.PACKET_ID, new UpdateRelayTriggerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredCounterBlockPacket.PACKET_ID, UpdateTriggeredCounterBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredCounterBlockPacket.PACKET_ID, new UpdateTriggeredCounterBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateDelayTriggerBlockPacket.PACKET_ID, UpdateDelayTriggerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateDelayTriggerBlockPacket.PACKET_ID, new UpdateDelayTriggerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredDisplayBlockPacket.PACKET_ID, UpdateTriggeredDisplayBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredDisplayBlockPacket.PACKET_ID, new UpdateTriggeredDisplayBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredEntityRemoverBlockPacket.PACKET_ID, UpdateTriggeredEntityRemoverBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredEntityRemoverBlockPacket.PACKET_ID, new UpdateTriggeredEntityRemoverBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredRNGBlockPacket.PACKET_ID, UpdateTriggeredRNGBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredRNGBlockPacket.PACKET_ID, new UpdateTriggeredRNGBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateInteractiveLootBlockPacket.PACKET_ID, UpdateInteractiveLootBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateInteractiveLootBlockPacket.PACKET_ID, new UpdateInteractiveLootBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateUseRelayBlockPacket.PACKET_ID, UpdateUseRelayBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateUseRelayBlockPacket.PACKET_ID, new UpdateUseRelayBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(LeaveHouseFromHousingScreenPacket.PACKET_ID, LeaveHouseFromHousingScreenPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(LeaveHouseFromHousingScreenPacket.PACKET_ID, new LeaveHouseFromHousingScreenPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateMimicBlockPacket.PACKET_ID, UpdateMimicBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateMimicBlockPacket.PACKET_ID, new UpdateMimicBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdatePlayerDetectorBlockPacket.PACKET_ID, UpdatePlayerDetectorBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdatePlayerDetectorBlockPacket.PACKET_ID, new UpdatePlayerDetectorBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateBossControllerBlockPacket.PACKET_ID, UpdateBossControllerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateBossControllerBlockPacket.PACKET_ID, new UpdateBossControllerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredSpawnerBlockPacket.PACKET_ID, UpdateTriggeredSpawnerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredSpawnerBlockPacket.PACKET_ID, new UpdateTriggeredSpawnerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredVillagerSpawnerBlockPacket.PACKET_ID, UpdateTriggeredVillagerSpawnerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredVillagerSpawnerBlockPacket.PACKET_ID, new UpdateTriggeredVillagerSpawnerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateLocationControlBlockPacket.PACKET_ID, UpdateLocationControlBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateLocationControlBlockPacket.PACKET_ID, new UpdateLocationControlBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateEntranceDelegationBlockPacket.PACKET_ID, UpdateEntranceDelegationBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateEntranceDelegationBlockPacket.PACKET_ID, new UpdateEntranceDelegationBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateAreaBlockPacket.PACKET_ID, UpdateAreaBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateAreaBlockPacket.PACKET_ID, new UpdateAreaBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateAreaFillerBlockPacket.PACKET_ID, UpdateAreaFillerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateAreaFillerBlockPacket.PACKET_ID, new UpdateAreaFillerBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredBeaconBlockPacket.PACKET_ID, UpdateTriggeredBeaconBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredBeaconBlockPacket.PACKET_ID, new UpdateTriggeredBeaconBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(UpdateTriggeredAdvancementCheckerBlockPacket.PACKET_ID, UpdateTriggeredAdvancementCheckerBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTriggeredAdvancementCheckerBlockPacket.PACKET_ID, new UpdateTriggeredAdvancementCheckerBlockPacketReceiver());

		// --- teleporter packets

		PayloadTypeRegistry.playC2S().register(UpdateTeleporterBlockPacket.PACKET_ID, UpdateTeleporterBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateTeleporterBlockPacket.PACKET_ID, new UpdateTeleporterBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(SetManualResetLocationControlBlockPacket.PACKET_ID, SetManualResetLocationControlBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SetManualResetLocationControlBlockPacket.PACKET_ID, new SetManualResetLocationControlBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(TeleportFromTeleporterBlockPacket.PACKET_ID, TeleportFromTeleporterBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TeleportFromTeleporterBlockPacket.PACKET_ID, new TeleportFromTeleporterBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(TeleportToTeamPacket.PACKET_ID, TeleportToTeamPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TeleportToTeamPacket.PACKET_ID, new TeleportToTeamPacketReceiver());

		// --- shop packets

		PayloadTypeRegistry.playC2S().register(UpdateShopBlockPacket.PACKET_ID, UpdateShopBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateShopBlockPacket.PACKET_ID, new UpdateShopBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(TradeWithShopPacket.PACKET_ID, TradeWithShopPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(TradeWithShopPacket.PACKET_ID, new TradeWithShopPacketReceiver());

		// --- dialogue packets

		PayloadTypeRegistry.playC2S().register(UpdateDialogueBlockPacket.PACKET_ID, UpdateDialogueBlockPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateDialogueBlockPacket.PACKET_ID, new UpdateDialogueBlockPacketReceiver());

		PayloadTypeRegistry.playC2S().register(OpenDialogueScreenPacket.PACKET_ID, OpenDialogueScreenPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenDialogueScreenPacket.PACKET_ID, new OpenDialogueScreenPacketReceiver());

		PayloadTypeRegistry.playC2S().register(DialogueAnswerPacket.PACKET_ID, DialogueAnswerPacket.PACKET_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DialogueAnswerPacket.PACKET_ID, new DialogueAnswerPacketReceiver());
	}
}
