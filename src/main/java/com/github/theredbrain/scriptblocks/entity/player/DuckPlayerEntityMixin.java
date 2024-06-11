package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.*;
import net.minecraft.text.Text;

public interface DuckPlayerEntityMixin {

    void scriptblocks$sendAnnouncement(Text announcement);

    void scriptblocks$openHousingScreen();
    void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock);
    void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock);
    void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock);
    void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock);
    void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock);
    void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock);
    void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock);
    void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock);
    void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock);
//    void scriptblocks$openDialogueScreen(DialogueBlockEntity dialogueBlockEntity, @Nullable Dialogue dialogue);
    void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity);
    void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity);
    void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity);
    void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock);
    void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity);
}
