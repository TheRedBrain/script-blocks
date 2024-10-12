package com.github.theredbrain.scriptblocks.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
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

	void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity);

	void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity);

	void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity);

	void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock);

	void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity);

	void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity);
}
