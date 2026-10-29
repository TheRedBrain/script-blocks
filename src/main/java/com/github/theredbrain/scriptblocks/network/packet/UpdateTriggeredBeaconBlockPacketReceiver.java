package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateTriggeredBeaconBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredBeaconBlockPacket> {
	@Override
	public void receive(UpdateTriggeredBeaconBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredBeaconBlockPosition = payload.triggeredBeaconBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.applicationAreaDimensions();
		BlockPos areaPositionOffset = payload.applicationAreaPositionOffset();

		String appliedStatusEffectIdentifier = payload.appliedStatusEffectIdentifier();
		int appliedStatusEffectAmplifier = payload.appliedStatusEffectAmplifier();
		int appliedStatusEffectDuration = payload.appliedStatusEffectDuration();
		boolean appliedStatusEffectAmbient = payload.appliedStatusEffectAmbient();
		boolean appliedStatusEffectShowParticles = payload.appliedStatusEffectShowParticles();
		boolean appliedStatusEffectShowIcon = payload.appliedStatusEffectShowIcon();


		boolean triggered = payload.triggered();

		TriggeredBeaconBlockEntity.TriggeredMode triggeredMode = TriggeredBeaconBlockEntity.TriggeredMode.byName(payload.triggeredMode()).orElse(TriggeredBeaconBlockEntity.TriggeredMode.CONTINUOUS);

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(triggeredBeaconBlockPosition);
		BlockState blockState = serverLevel.getBlockState(triggeredBeaconBlockPosition);

		if (blockEntity instanceof TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
			triggeredBeaconBlockEntity.reset();
			triggeredBeaconBlockEntity.setShowArea(showArea);
			if (!triggeredBeaconBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_beacon_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_beacon_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAppliedStatusEffectIdentifier(appliedStatusEffectIdentifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_beacon_block.appliedStatusEffectIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAppliedStatusEffectAmplifier(appliedStatusEffectAmplifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_beacon_block.appliedStatusEffectAmplifier.invalid"), false);
				updateSuccessful = false;
			}
			triggeredBeaconBlockEntity.setAppliedStatusEffectDuration(appliedStatusEffectDuration);
			triggeredBeaconBlockEntity.setAppliedStatusEffectAmbient(appliedStatusEffectAmbient);
			triggeredBeaconBlockEntity.setAppliedStatusEffectShowParticles(appliedStatusEffectShowParticles);
			triggeredBeaconBlockEntity.setAppliedStatusEffectShowIcon(appliedStatusEffectShowIcon);

			triggeredBeaconBlockEntity.setTriggered(triggered);

			triggeredBeaconBlockEntity.setTriggeredMode(triggeredMode);

			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredBeaconBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(triggeredBeaconBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
