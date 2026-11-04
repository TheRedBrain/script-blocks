package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListMessagingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.AreaBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.AreaFillerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.BossControllerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.CopyDataBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.CreativeHousingScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.CreativeTeleporterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataModificationBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataRelayBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataSavingBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataWritingBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DelayTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DialogueBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.EntranceDelegationBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.HousingScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.InteractiveLootBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.InteractiveTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.JigsawPlacerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.LocationControlBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.MimicBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.PVPControllerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.PlayerDetectorBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.RedstoneTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.RelayTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.ShopBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.SpawnPointDelegationBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.StatusEffectManipulationBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TeamControllerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredAdvancementCheckerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredBeaconBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredCounterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredDamageDealingBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredDisplayBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredEntityRemoverBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredRNGBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredSpawnerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredVillagerSpawnerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeringRespawnAnchorBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TwoAreasPlayerDetectorBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.UUIDListMessagingBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.UUIDListRelayBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.UseRelayBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.UseRelayChestBlockScreen;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer implements DuckPlayerEntityMixin {

	@Shadow
	@Final
	protected Minecraft minecraft;

	public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Override
	public void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity) {
		this.minecraft.setScreen(new CreativeHousingScreen(housingBlockEntity));
	}

	@Override
	public void scriptblocks$openHousingScreen() {
		Optional<BlockPos> optionalBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(this);
		if (optionalBlockPos.isPresent() && this.minecraft.getSingleplayerServer() != null && this.level().getBlockEntity(optionalBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
			int currentPermissionLevel;

			if (this.hasEffect(ScriptBlocksStatusEffects.HOUSING_OWNER_EFFECT)) {
				currentPermissionLevel = 0;
			} else if (this.hasEffect(ScriptBlocksStatusEffects.HOUSING_CO_OWNER_EFFECT)) {
				currentPermissionLevel = 1;
			} else if (this.hasEffect(ScriptBlocksStatusEffects.HOUSING_TRUSTED_EFFECT)) {
				currentPermissionLevel = 2;
			} else if (this.hasEffect(ScriptBlocksStatusEffects.HOUSING_GUEST_EFFECT)) {
				currentPermissionLevel = 3;
			} else if (this.hasEffect(ScriptBlocksStatusEffects.HOUSING_STRANGER_EFFECT)) {
				currentPermissionLevel = 4;
			} else {
				this.sendOverlayMessage(Component.translatable("gui.housing_screen.not_in_a_house"));
				return;
			}
			this.minecraft.setScreen(new HousingScreen(housingBlockEntity, currentPermissionLevel));
		} else {
			this.sendOverlayMessage(Component.translatable("gui.housing_screen.not_in_a_house"));
		}
	}

	@Override
	public void scriptblocks$openTeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity) {
		this.minecraft.setScreen(new TeamControllerBlockScreen(teamControllerBlockEntity));
	}

	@Override
	public void scriptblocks$openTriggeredBeaconBlockScreen(TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
		this.minecraft.setScreen(new TriggeredBeaconBlockScreen(triggeredBeaconBlockEntity));
	}

	@Override
	public void scriptblocks$openShopBlockScreen(ShopBlockEntity shopBlockEntity) {
		this.minecraft.setScreen(new ShopBlockScreen(shopBlockEntity));
	}

	@Override
	public void scriptblocks$openDialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
		this.minecraft.setScreen(new DialogueBlockScreen(dialogueBlockEntity));
	}

//	@Override
//	public void scriptblocks$openDialogueScreen(Dialogue dialogue, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks) {
//		this.minecraft.setScreen(new DialogueScreen(dialogue, dialogueUsedBlocks, dialogueTriggeredBlocks));
//	}

	@Override
	public void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
		this.minecraft.setScreen(new JigsawPlacerBlockScreen(jigsawPlacerBlock));
	}

	@Override
	public void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
		this.minecraft.setScreen(new RedstoneTriggerBlockScreen(redstoneTriggerBlock));
	}

	@Override
	public void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
		this.minecraft.setScreen(new RelayTriggerBlockScreen(relayTriggerBlock));
	}

	@Override
	public void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
		this.minecraft.setScreen(new TriggeredCounterBlockScreen(triggeredCounterBlock));
	}

	@Override
	public void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
		this.minecraft.setScreen(new DelayTriggerBlockScreen(delayTriggerBlock));
	}

	@Override
	public void scriptblocks$openTriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlockEntity) {
		this.minecraft.setScreen(new TriggeredDisplayBlockScreen(triggeredDisplayBlockEntity));
	}

	@Override
	public void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity) {
		this.minecraft.setScreen(new CreativeTeleporterBlockScreen(teleporterBlockEntity));
	}

	@Override
	public void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
		this.minecraft.setScreen(new UseRelayBlockScreen(useRelayBlock));
	}

	@Override
	public void scriptblocks$openUseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlock) {
		this.minecraft.setScreen(new UseRelayChestBlockScreen(useRelayChestBlock));
	}

	@Override
	public void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
		this.minecraft.setScreen(new TriggeredSpawnerBlockScreen(triggeredSpawnerBlock));
	}

	@Override
	public void scriptblocks$openTriggeredVillagerSpawnerBlockScreen(TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock) {
		this.minecraft.setScreen(new TriggeredVillagerSpawnerBlockScreen(triggeredVillagerSpawnerBlock));
	}

	@Override
	public void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock) {
		this.minecraft.setScreen(new MimicBlockScreen(mimicBlock));
	}

	@Override
	public void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
		this.minecraft.setScreen(new LocationControlBlockScreen(locationControlBlock));
	}

	@Override
	public void scriptblocks$openSpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
		this.minecraft.setScreen(new SpawnPointDelegationBlockScreen(spawnPointDelegationBlockEntity));
	}

	@Deprecated
	@Override
	public void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
		this.minecraft.setScreen(new EntranceDelegationBlockScreen(entranceDelegationBlockEntity));
	}

	@Deprecated
	@Override
	public void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
		this.minecraft.setScreen(new AreaBlockScreen(areaBlockEntity));
	}

	@Override
	public void scriptblocks$openAreaFillerBlockScreen(AreaFillerBlockEntity areaFillerBlockEntity) {
		this.minecraft.setScreen(new AreaFillerBlockScreen(areaFillerBlockEntity));
	}

	@Override
	public void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
		this.minecraft.setScreen(new BossControllerBlockScreen(bossControllerBlockEntity));
	}

	@Override
	public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
		this.minecraft.setScreen(new TriggeredAdvancementCheckerBlockScreen(triggeredAdvancementCheckerBlock));
	}

	@Override
	public void scriptblocks$openTriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
		this.minecraft.setScreen(new TriggeredRNGBlockScreen(triggeredRNGBlockEntity));
	}

	@Deprecated
	@Override
	public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
		this.minecraft.setScreen(new InteractiveLootBlockScreen(interactiveLootBlockEntity));
	}

	@Override
	public void scriptblocks$openInteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
		this.minecraft.setScreen(new InteractiveTriggerBlockScreen(interactiveTriggerBlockEntity));
	}

//	@Override
//	public void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
//		this.minecraft.setScreen(new DataAccessBlockScreen(dataAccessBlockEntity));
//	}

	@Override
	public void scriptblocks$openCopyDataBlockScreen(CopyDataBlockEntity copyDataBlockEntity) {
		this.minecraft.setScreen(new CopyDataBlockScreen(copyDataBlockEntity));
	}

	@Override
	public void scriptblocks$openDataWritingBlockScreen(DataWritingBlockEntity dataWritingBlockEntity) {
		this.minecraft.setScreen(new DataWritingBlockScreen(dataWritingBlockEntity));
	}

	@Override
	public void scriptblocks$openDataModificationBlockScreen(DataModificationBlockEntity dataModificationBlockEntity) {
		this.minecraft.setScreen(new DataModificationBlockScreen(dataModificationBlockEntity));
	}

	@Override
	public void scriptblocks$openDataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
		this.minecraft.setScreen(new DataRelayBlockScreen(dataRelayBlockEntity));
	}

	@Override
	public void scriptblocks$openDataSavingBlockScreen(DataSavingBlockEntity dataSavingBlockEntity) {
		this.minecraft.setScreen(new DataSavingBlockScreen(dataSavingBlockEntity));
	}

	@Override
	public void scriptblocks$openTriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
		this.minecraft.setScreen(new TriggeredDamageDealingBlockScreen(triggeredDamageDealingBlockEntity));
	}

	@Override
	public void scriptblocks$openTriggeredEntityRemoverBlockScreen(TriggeredEntityRemoverBlockEntity triggeredEntityRemoverBlockEntity) {
		this.minecraft.setScreen(new TriggeredEntityRemoverBlockScreen(triggeredEntityRemoverBlockEntity));
	}

	@Override
	public void scriptblocks$openTriggeringRespawnAnchorBlockScreen(TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
		this.minecraft.setScreen(new TriggeringRespawnAnchorBlockScreen(triggeringRespawnAnchorBlockEntity));
	}

	@Override
	public void scriptblocks$openPlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlockEntity) {
		this.minecraft.setScreen(new PlayerDetectorBlockScreen(playerDetectorBlockEntity));
	}

	@Override
	public void scriptblocks$openTwoAreasPlayerDetectorBlockScreen(TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		this.minecraft.setScreen(new TwoAreasPlayerDetectorBlockScreen(twoAreasPlayerDetectorBlockEntity));
	}

	@Override
	public void scriptblocks$openPVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity) {
		this.minecraft.setScreen(new PVPControllerBlockScreen(pvpControllerBlockEntity));
	}

	@Override
	public void scriptblocks$openStatusEffectManipulationBlockScreen(StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
		this.minecraft.setScreen(new StatusEffectManipulationBlockScreen(statusEffectManipulationBlockEntity));
	}

	@Override
	public void scriptblocks$openUUIDListMessagingBlockScreen(UUIDListMessagingBlockEntity uuidListMessagingBlockEntity) {
		this.minecraft.setScreen(new UUIDListMessagingBlockScreen(uuidListMessagingBlockEntity));
	}

	@Override
	public void scriptblocks$openUUIDListRelayBlockScreen(UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
		this.minecraft.setScreen(new UUIDListRelayBlockScreen(uuidListRelayBlockEntity));
	}

//	@Override
//	public void scriptblocks$openLootableVaultBlockScreen(LootableVaultBlockEntity lootableVaultBlockEntity) {
//		this.minecraft.setScreen(new LootableVaultBlockScreen(lootableVaultBlockEntity));
//	}
}
