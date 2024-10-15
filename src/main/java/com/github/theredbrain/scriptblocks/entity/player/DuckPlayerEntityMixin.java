package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DuckPlayerEntityMixin {

    @Nullable BlockPos scriptblocks$getCurrentHousingBlockPosition();

    void scriptblocks$setCurrentHousingBlockPosition(@Nullable BlockPos currentHousingBlockPosition);

    @Nullable MutablePair<String, BlockPos> scriptblocks$getLocationAccessPosition();

    void scriptblocks$setLocationAccessPosition(@Nullable MutablePair<String, BlockPos> locationAccessPosition);

    void scriptblocks$sendAnnouncement(Text announcement);

    void scriptblocks$openCreativeHousingScreen(HousingBlockEntity housingBlockEntity);

    void scriptblocks$openHousingScreen();

    void scriptblocks$openShopBlockScreen(ShopBlockEntity shopBlockEntity);

    void scriptblocks$openDialogueBlockScreen(DialogueBlockEntity dialogueBlockEntity);

    void scriptblocks$openDialogueScreen(Dialogue dialogue, List<MutablePair<String, BlockPos>> dialogueUsedBlocks, List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocks);

    void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock);

    void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock);

    void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock);

    void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock);

    void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock);

    void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity);

    void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock);

    void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock);

    void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock);

    void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock);

    void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity);

    void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity);

    void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity);

    void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock);

    void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity);

    void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity);

    void scriptblocks$openDataRelayBlockScreen(DataRelayBlockEntity dataRelayBlockEntity);
}
