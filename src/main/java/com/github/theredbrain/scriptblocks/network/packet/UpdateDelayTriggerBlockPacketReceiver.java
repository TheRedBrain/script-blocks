package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateDelayTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDelayTriggerBlockPacket> {
	@Override
	public void receive(UpdateDelayTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos delayTriggerBlockPosition = payload.delayTriggerBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		int triggerDelay = payload.triggerDelay();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(delayTriggerBlockPosition);
		BlockState blockState = world.getBlockState(delayTriggerBlockPosition);

		if (blockEntity instanceof DelayTriggerBlockEntity delayTriggerBlockEntity) {
			delayTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			delayTriggerBlockEntity.setTriggerDelay(triggerDelay);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			delayTriggerBlockEntity.setChanged();
			world.sendBlockUpdated(delayTriggerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
