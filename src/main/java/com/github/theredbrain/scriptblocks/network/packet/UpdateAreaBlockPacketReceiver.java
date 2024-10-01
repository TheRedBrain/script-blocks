package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateAreaBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateAreaBlockPacket> {
	@Override
	public void receive(UpdateAreaBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		AreaBlockEntity.MessageMode messageMode = AreaBlockEntity.MessageMode.valueOf(payload.messageMode());
		AreaBlockEntity.TriggerMode triggerMode = AreaBlockEntity.TriggerMode.valueOf(payload.triggerMode());
		AreaBlockEntity.TriggeredMode triggeredMode = AreaBlockEntity.TriggeredMode.valueOf(payload.triggeredMode());
		int timer = payload.timer();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(areaBlockPosition);
		BlockState blockState = world.getBlockState(areaBlockPosition);

		if (blockEntity instanceof AreaBlockEntity areaBlockEntity) {
			areaBlockEntity.reset();
			areaBlockEntity.setShowArea(showArea);
			if (!areaBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAppliedStatusEffectIdentifier(appliedStatusEffectIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.appliedStatusEffectIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaBlockEntity.setAppliedStatusEffectAmplifier(appliedStatusEffectAmplifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_block.appliedStatusEffectAmplifier.invalid"), false);
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
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			areaBlockEntity.markDirty();
			world.updateListeners(areaBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
