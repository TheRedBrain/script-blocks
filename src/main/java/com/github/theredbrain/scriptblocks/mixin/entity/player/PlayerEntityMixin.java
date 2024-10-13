package com.github.theredbrain.scriptblocks.mixin.entity.player;

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
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public void scriptblocks$openHousingScreen() {
	}

	@Override
	public void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
	}

	@Override
	public void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
	}

	@Override
	public void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
	}

	@Override
	public void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
	}

	@Override
	public void scriptblocks$openCreativeTeleporterBlockScreen(TeleporterBlockEntity teleporterBlockEntity) {
	}

	@Override
	public void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
	}

	@Override
	public void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
	}

	@Override
	public void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock) {
	}

	@Override
	public void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
	}

	@Override
	public void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
	}

	@Override
	public void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
	}

	@Override
	public void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
	}

	@Override
	public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
	}

	@Override
	public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
	}

	@Override
	public void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
	}
}
