package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
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
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
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
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface DuckPlayerEntityMixin {

	Optional<BlockPos> scriptblocks$getCurrentHousingBlockPosition();

	void scriptblocks$setCurrentHousingBlockPosition(Optional<BlockPos> currentHousingBlockPosition);

	@Nullable MutablePair<String, BlockPos> scriptblocks$getLocationAccessPosition();

	void scriptblocks$setLocationAccessPosition(@Nullable MutablePair<String, BlockPos> locationAccessPosition);

	Optional<BlockPos> scriptblocks$getCurrentPVPControllerBlockPosition();

	void scriptblocks$setCurrentPVPControllerBlockPosition(Optional<BlockPos> currentPVPControllerBlockPosition);

	void scriptblocks$sendAnnouncement(Text announcement);

	void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity);

	void scriptblocks$openHousingScreen();

	void scriptblocks$openTeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity);

	void scriptblocks$openTriggeredBeaconBlockScreen(TriggeredBeaconBlockEntity triggeredBeaconBlockEntity);

	void scriptblocks$openShopBlockScreen(ShopBlockEntity shopBlockEntity);

	void scriptblocks$openDialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity);

//	void scriptblocks$openDialogueScreen(Dialogue dialogue, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks);

	void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock);

	void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock);

	void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock);

	void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock);

	void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock);

	void scriptblocks$openTriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlockEntity);

	void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity);

	void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock);

	void scriptblocks$openUseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlock);

	void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock);

	void scriptblocks$openTriggeredVillagerSpawnerBlockScreen(TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock);

	void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock);

	void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock);

	default void scriptblocks$openSpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {}

	void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity);

	void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity);

	void scriptblocks$openAreaFillerBlockScreen(AreaFillerBlockEntity areaFillerBlockEntity);

	void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity);

	void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock);

	void scriptblocks$openTriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity);

	void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity);

	void scriptblocks$openInteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity);

//	void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity);

	void scriptblocks$openCopyDataBlockScreen(CopyDataBlockEntity copyDataBlockEntity);

	void scriptblocks$openDataWritingBlockScreen(DataWritingBlockEntity dataWritingBlockEntity);

	void scriptblocks$openDataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity);

	void scriptblocks$openDataSavingBlockScreen(DataSavingBlockEntity dataSavingBlockEntity);

	void scriptblocks$openTriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity);

	void scriptblocks$openTriggeredEntityRemoverBlockScreen(TriggeredEntityRemoverBlockEntity triggeredEntityRemoverBlockEntity);

	void scriptblocks$openPlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlockEntity);

	void scriptblocks$openPVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity);

//	void scriptblocks$openLootableVaultBlockScreen(LootableVaultBlockEntity lootableVaultBlockEntity);
}
