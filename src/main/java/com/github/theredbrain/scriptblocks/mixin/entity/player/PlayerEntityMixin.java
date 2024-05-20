package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.block.entity.*;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

    @Shadow public abstract PlayerInventory getInventory();

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Shadow public abstract ItemStack getEquippedStack(EquipmentSlot slot);

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

//    @Override
//    public void scriptblocks$openDialogueScreen(DialogueBlockEntity dialogueBlockEntity, @Nullable Dialogue dialogue) {
//    }

    @Override
    public void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
    }

    @Override
    public void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
    }

    @Override
    public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
    }

    @Override
    public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
    }
}
