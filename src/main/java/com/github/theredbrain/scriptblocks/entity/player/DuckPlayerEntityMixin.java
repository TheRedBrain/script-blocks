package com.github.theredbrain.scriptblocks.entity.player;

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
import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface DuckPlayerEntityMixin {

	Optional<BlockPos> scriptblocks$getCurrentHousingBlockPosition();

	void scriptblocks$setCurrentHousingBlockPosition(Optional<BlockPos> currentHousingBlockPosition);

	@Nullable MutablePair<String, BlockPos> scriptblocks$getLocationAccessPosition();

	void scriptblocks$setLocationAccessPosition(@Nullable MutablePair<String, BlockPos> locationAccessPosition);

	Optional<BlockPos> scriptblocks$getCurrentPVPControllerBlockPosition();

	void scriptblocks$setCurrentPVPControllerBlockPosition(Optional<BlockPos> currentPVPControllerBlockPosition);

	void scriptblocks$sendAnnouncement(Text announcement);

	default void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity) {
	}

	default void scriptblocks$openHousingScreen() {
	}

	default void scriptblocks$openTeamControllerBlockScreen(TeamControllerBlockEntity teamControllerBlockEntity) {
	}

	default void scriptblocks$openTriggeredBeaconBlockScreen(TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
	}

	default void scriptblocks$openShopBlockScreen(ShopBlockEntity shopBlockEntity) {
	}

	default void scriptblocks$openDialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity) {
	}

//	default void scriptblocks$openDialogueScreen(Dialogue dialogue, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks);

	default void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
	}

	default void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
	}

	default void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
	}

	default void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
	}

	default void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
	}

	default void scriptblocks$openTriggeredDisplayBlockScreen(TriggeredDisplayBlockEntity triggeredDisplayBlockEntity) {
	}

	default void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity) {
	}

	default void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
	}

	default void scriptblocks$openUseRelayChestBlockScreen(UseRelayChestBlockEntity useRelayChestBlock) {
	}

	default void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
	}

	default void scriptblocks$openTriggeredVillagerSpawnerBlockScreen(TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlock) {
	}

	default void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock) {
	}

	default void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
	}

	default void scriptblocks$openSpawnPointDelegationBlockScreen(SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
	}

	@Deprecated
	default void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
	}

	@Deprecated
	default void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
	}

	default void scriptblocks$openAreaFillerBlockScreen(AreaFillerBlockEntity areaFillerBlockEntity) {
	}

	default void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
	}

	default void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
	}

	default void scriptblocks$openTriggeredRNGBlockScreen(TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
	}

	@Deprecated
	default void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
	}

	default void scriptblocks$openInteractiveTriggerBlockScreen(InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
	}

//	default void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
//	}

	default void scriptblocks$openCopyDataBlockScreen(CopyDataBlockEntity copyDataBlockEntity) {
	}

	default void scriptblocks$openDataWritingBlockScreen(DataWritingBlockEntity dataWritingBlockEntity) {
	}

	default void scriptblocks$openDataModificationBlockScreen(DataModificationBlockEntity dataModificationBlockEntity) {
	}

	default void scriptblocks$openDataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity) {
	}

	default void scriptblocks$openDataSavingBlockScreen(DataSavingBlockEntity dataSavingBlockEntity) {
	}

	default void scriptblocks$openTriggeredDamageDealingBlockScreen(TriggeredDamageDealingBlockEntity triggeredDamageDealingBlockEntity) {
	}

	default void scriptblocks$openTriggeredEntityRemoverBlockScreen(TriggeredEntityRemoverBlockEntity triggeredEntityRemoverBlockEntity) {
	}

	default void scriptblocks$openPlayerDetectorBlockScreen(PlayerDetectorBlockEntity playerDetectorBlockEntity) {
	}

	default void scriptblocks$openPVPControllerBlockScreen(PVPControllerBlockEntity pvpControllerBlockEntity) {
	}

	default void scriptblocks$openStatusEffectManipulationBlockScreen(StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity) {
	}

	default void scriptblocks$openUUIDListRelayBlockScreen(UUIDListRelayBlockEntity uuidListRelayBlockEntity) {
	}
}
