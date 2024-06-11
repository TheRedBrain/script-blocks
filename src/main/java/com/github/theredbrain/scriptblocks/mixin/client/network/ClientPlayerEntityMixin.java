package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.block.entity.*;
import com.github.theredbrain.scriptblocks.client.gui.screen.ingame.*;
import com.github.theredbrain.scriptblocks.client.network.message.DuckMessageHandlerMixin;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ComponentsRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(value = ClientPlayerEntity.class,priority = 950)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DuckPlayerEntityMixin {

    @Shadow @Final protected MinecraftClient client;
    @Shadow public Input input;

    @Shadow public abstract boolean isUsingItem();

    @Shadow public abstract float getPitch(float tickDelta);

    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public void scriptblocks$sendAnnouncement(Text announcement) {
        ((DuckMessageHandlerMixin)this.client.getMessageHandler()).scriptblocks$onAnnouncement(announcement);
    }

    @Override
    public void scriptblocks$openHousingScreen() {
        HousingBlockEntity housingBlockEntity = null;
        if (this.client.getServer() != null && this.client.world != null && this.client.world.getBlockEntity(ComponentsRegistry.CURRENT_HOUSING_BLOCK_POS.get(this).getValue()) instanceof HousingBlockEntity housingBlockEntity1) {
            housingBlockEntity = housingBlockEntity1;
        }
        int currentPermissionLevel;
        if (this.hasStatusEffect(StatusEffectsRegistry.HOUSING_OWNER_EFFECT) && housingBlockEntity != null) {
            currentPermissionLevel = 0;
        } else if (this.hasStatusEffect(StatusEffectsRegistry.HOUSING_CO_OWNER_EFFECT) && housingBlockEntity != null) {
            currentPermissionLevel = 1;
        } else if (this.hasStatusEffect(StatusEffectsRegistry.HOUSING_TRUSTED_EFFECT) && housingBlockEntity != null) {
            currentPermissionLevel = 2;
        } else if (this.hasStatusEffect(StatusEffectsRegistry.HOUSING_GUEST_EFFECT) && housingBlockEntity != null) {
            currentPermissionLevel = 3;
        } else if (this.hasStatusEffect(StatusEffectsRegistry.HOUSING_STRANGER_EFFECT) && housingBlockEntity != null) {
            currentPermissionLevel = 4;
        } else if (this.isCreative()) {
            currentPermissionLevel = 5;
        } else {
            this.sendMessage(Text.translatable("gui.housing_screen.not_in_a_house"), true);
            return;
        }
        this.client.setScreen(new HousingScreen(housingBlockEntity, currentPermissionLevel, this.isCreative()));
    }

    @Override
    public void scriptblocks$openJigsawPlacerBlockScreen(JigsawPlacerBlockEntity jigsawPlacerBlock) {
        this.client.setScreen(new JigsawPlacerBlockScreen(jigsawPlacerBlock));
    }

    @Override
    public void scriptblocks$openRedstoneTriggerBlockScreen(RedstoneTriggerBlockEntity redstoneTriggerBlock) {
        this.client.setScreen(new RedstoneTriggerBlockScreen(redstoneTriggerBlock));
    }

    @Override
    public void scriptblocks$openRelayTriggerBlockScreen(RelayTriggerBlockEntity relayTriggerBlock) {
        this.client.setScreen(new RelayTriggerBlockScreen(relayTriggerBlock));
    }

    @Override
    public void scriptblocks$openTriggeredCounterBlockScreen(TriggeredCounterBlockEntity triggeredCounterBlock) {
        this.client.setScreen(new TriggeredCounterBlockScreen(triggeredCounterBlock));
    }

    @Override
    public void scriptblocks$openDelayTriggerBlockScreen(DelayTriggerBlockEntity delayTriggerBlock) {
        this.client.setScreen(new DelayTriggerBlockScreen(delayTriggerBlock));
    }

    @Override
    public void scriptblocks$openUseRelayBlockScreen(UseRelayBlockEntity useRelayBlock) {
        this.client.setScreen(new UseRelayBlockScreen(useRelayBlock));
    }

    @Override
    public void scriptblocks$openTriggeredSpawnerBlockScreen(TriggeredSpawnerBlockEntity triggeredSpawnerBlock) {
        this.client.setScreen(new TriggeredSpawnerBlockScreen(triggeredSpawnerBlock));
    }

    @Override
    public void scriptblocks$openMimicBlockScreen(MimicBlockEntity mimicBlock) {
        this.client.setScreen(new MimicBlockScreen(mimicBlock));
    }

    @Override
    public void scriptblocks$openLocationControlBlockScreen(LocationControlBlockEntity locationControlBlock) {
        this.client.setScreen(new LocationControlBlockScreen(locationControlBlock));
    }

    @Override
    public void scriptblocks$openEntranceDelegationBlockScreen(EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
        this.client.setScreen(new EntranceDelegationBlockScreen(entranceDelegationBlockEntity));
    }

    @Override
    public void scriptblocks$openAreaBlockScreen(AreaBlockEntity areaBlockEntity) {
        this.client.setScreen(new AreaBlockScreen(areaBlockEntity));
    }

    @Override
    public void scriptblocks$openBossControllerBlockScreen(BossControllerBlockEntity bossControllerBlockEntity) {
        this.client.setScreen(new BossControllerBlockScreen(bossControllerBlockEntity));
    }

    @Override
    public void scriptblocks$openTriggeredAdvancementCheckerBlockScreen(TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlock) {
        this.client.setScreen(new TriggeredAdvancementCheckerBlockScreen(triggeredAdvancementCheckerBlock));
    }

    @Override
    public void scriptblocks$openInteractiveLootBlockScreen(InteractiveLootBlockEntity interactiveLootBlockEntity) {
        this.client.setScreen(new InteractiveLootBlockScreen(interactiveLootBlockEntity));
    }
}
