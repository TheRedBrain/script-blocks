package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class UpdateTriggeredBeaconBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredBeaconBlockPacket> {
	@Override
	public void receive(UpdateTriggeredBeaconBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredBeaconBlockPosition);
		BlockState blockState = world.getBlockState(triggeredBeaconBlockPosition);

		if (blockEntity instanceof TriggeredBeaconBlockEntity triggeredBeaconBlockEntity) {
			triggeredBeaconBlockEntity.reset();
			triggeredBeaconBlockEntity.setShowArea(showArea);
			if (!triggeredBeaconBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_beacon_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_beacon_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAppliedStatusEffectIdentifier(appliedStatusEffectIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_beacon_block.appliedStatusEffectIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredBeaconBlockEntity.setAppliedStatusEffectAmplifier(appliedStatusEffectAmplifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_beacon_block.appliedStatusEffectAmplifier.invalid"), false);
				updateSuccessful = false;
			}
			triggeredBeaconBlockEntity.setAppliedStatusEffectDuration(appliedStatusEffectDuration);
			triggeredBeaconBlockEntity.setAppliedStatusEffectAmbient(appliedStatusEffectAmbient);
			triggeredBeaconBlockEntity.setAppliedStatusEffectShowParticles(appliedStatusEffectShowParticles);
			triggeredBeaconBlockEntity.setAppliedStatusEffectShowIcon(appliedStatusEffectShowIcon);

			triggeredBeaconBlockEntity.setTriggered(triggered);

			triggeredBeaconBlockEntity.setTriggeredMode(triggeredMode);

			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredBeaconBlockEntity.markDirty();
			world.updateListeners(triggeredBeaconBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
