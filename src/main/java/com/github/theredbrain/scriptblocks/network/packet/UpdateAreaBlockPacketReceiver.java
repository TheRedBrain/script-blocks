package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

@Deprecated
public class UpdateAreaBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateAreaBlockPacket> {
	@Override
	public void receive(UpdateAreaBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			return;
		}

		BlockPos areaBlockPosition = payload.areaBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.applicationAreaDimensions();
		BlockPos areaPositionOffset = payload.applicationAreaPositionOffset();

		String appliedStatusEffectIdentifier = payload.appliedStatusEffectIdentifier();
		int appliedStatusEffectAmplifier = payload.appliedStatusEffectAmplifier();
		boolean appliedStatusEffectAmbient = payload.appliedStatusEffectAmbient();
		boolean appliedStatusEffectShowParticles = payload.appliedStatusEffectShowParticles();
		boolean appliedStatusEffectShowIcon = payload.appliedStatusEffectShowIcon();


		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();
		boolean wasTriggered = payload.wasTriggered();

		String joinMessage = payload.joinMessage();
		String leaveMessage = payload.leaveMessage();
		String triggeredMessage = payload.triggeredMessage();

		AreaBlockEntity.MessageMode messageMode = AreaBlockEntity.MessageMode.byName(payload.messageMode()).orElse(AreaBlockEntity.MessageMode.OVERLAY);
		AreaBlockEntity.TriggerMode triggerMode = AreaBlockEntity.TriggerMode.byName(payload.triggerMode()).orElse(AreaBlockEntity.TriggerMode.ALWAYS);
		AreaBlockEntity.TriggeredMode triggeredMode = AreaBlockEntity.TriggeredMode.byName(payload.triggeredMode()).orElse(AreaBlockEntity.TriggeredMode.CONTINUOUS);
		int timer = payload.timer();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(areaBlockPosition);
		BlockState blockState = world.getBlockState(areaBlockPosition);

		if (blockEntity instanceof AreaBlockEntity areaBlockEntity) {
			areaBlockEntity.reset();
			areaBlockEntity.setShowArea(showArea);
			if (!areaBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("area_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("area_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAppliedStatusEffectIdentifier(appliedStatusEffectIdentifier)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("area_block.appliedStatusEffectIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAppliedStatusEffectAmplifier(appliedStatusEffectAmplifier)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("area_block.appliedStatusEffectAmplifier.invalid"), false);
				updateSuccessful = false;
			}
			areaBlockEntity.setAppliedStatusEffectAmbient(appliedStatusEffectAmbient);
			areaBlockEntity.setAppliedStatusEffectShowParticles(appliedStatusEffectShowParticles);
			areaBlockEntity.setAppliedStatusEffectShowIcon(appliedStatusEffectShowIcon);

			areaBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			areaBlockEntity.setWasTriggered(wasTriggered);

			areaBlockEntity.setJoinMessage(joinMessage);
			areaBlockEntity.setLeaveMessage(leaveMessage);
			areaBlockEntity.setTriggeredMessage(triggeredMessage);

			areaBlockEntity.setMessageMode(messageMode);
			areaBlockEntity.setTriggerMode(triggerMode);
			areaBlockEntity.setTriggeredMode(triggeredMode);
			areaBlockEntity.setMaxTimer(timer);

			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			areaBlockEntity.setChanged();
			world.sendBlockUpdated(areaBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
