package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
//import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
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
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.AreaBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.BossControllerBlockScreen;
//import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataAccessBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DataAccessBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.DelayTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.EntranceDelegationBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.HousingScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.InteractiveLootBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.JigsawPlacerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.LocationControlBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.MimicBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.RedstoneTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.RelayTriggerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredAdvancementCheckerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredCounterBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.TriggeredSpawnerBlockScreen;
import com.github.theredbrain.scriptblocks.gui.screen.ingame.UseRelayBlockScreen;
import com.github.theredbrain.scriptblocks.network.message.DuckMessageHandlerMixin;
import com.github.theredbrain.scriptblocks.registry.ComponentsRegistry;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements DuckPlayerEntityMixin {

	@Shadow
	@Final
	protected MinecraftClient client;

	@Shadow
	public abstract boolean isUsingItem();

	@Shadow
	public abstract float getPitch(float tickDelta);

	@Shadow
	public abstract void sendMessage(Text message, boolean overlay);

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
	}

	@Override
	public void scriptblocks$sendAnnouncement(Text announcement) {
		((DuckMessageHandlerMixin) this.client.getMessageHandler()).scriptblocks$onAnnouncement(announcement);
	}

	@Override
	public void scriptblocks$openHousingScreen() {
		HousingBlockEntity housingBlockEntity = null;
		if (this.client.getServer() != null && this.client.world != null && this.client.world.getBlockEntity(ComponentsRegistry.CURRENT_HOUSING_BLOCK_POS.get(this).getValue()) instanceof HousingBlockEntity housingBlockEntity1) {
			housingBlockEntity = housingBlockEntity1;
		}
		int currentPermissionLevel;

		RegistryEntry<StatusEffect> housingOwnerStatusEffect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_OWNER_EFFECT);
		RegistryEntry<StatusEffect> housingCoOwnerStatusEffect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_CO_OWNER_EFFECT);
		RegistryEntry<StatusEffect> housingTrustedStatusEffect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_TRUSTED_EFFECT);
		RegistryEntry<StatusEffect> housingGuestStatusEffect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_GUEST_EFFECT);
		RegistryEntry<StatusEffect> housingStrangerStatusEffect = Registries.STATUS_EFFECT.getEntry(StatusEffectsRegistry.HOUSING_STRANGER_EFFECT);
		if (this.hasStatusEffect(housingOwnerStatusEffect) && housingBlockEntity != null) {
			currentPermissionLevel = 0;
		} else if (this.hasStatusEffect(housingCoOwnerStatusEffect) && housingBlockEntity != null) {
			currentPermissionLevel = 1;
		} else if (this.hasStatusEffect(housingTrustedStatusEffect) && housingBlockEntity != null) {
			currentPermissionLevel = 2;
		} else if (this.hasStatusEffect(housingGuestStatusEffect) && housingBlockEntity != null) {
			currentPermissionLevel = 3;
		} else if (this.hasStatusEffect(housingStrangerStatusEffect) && housingBlockEntity != null) {
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

	@Override
	public void scriptblocks$openDataAccessBlockScreen(DataAccessBlockEntity dataAccessBlockEntity) {
		this.client.setScreen(new DataAccessBlockScreen(dataAccessBlockEntity));
	}
}
