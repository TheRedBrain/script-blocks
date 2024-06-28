package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateDelayTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPacketHandler<UpdateDelayTriggerBlockPacket> {
	@Override
	public void receive(UpdateDelayTriggerBlockPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

		if (!player.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos delayTriggerBlockPosition = packet.delayTriggerBlockPosition;

		BlockPos triggeredBlockPositionOffset = packet.triggeredBlockPositionOffset;

		boolean triggeredBlockResets = packet.triggeredBlockResets;

		int triggerDelay = packet.triggerDelay;

		World world = player.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(delayTriggerBlockPosition);
		BlockState blockState = world.getBlockState(delayTriggerBlockPosition);

		if (blockEntity instanceof DelayTriggerBlockEntity delayTriggerBlockEntity) {
			delayTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			if (!delayTriggerBlockEntity.setTriggerDelay(triggerDelay)) {
				player.sendMessage(Text.translatable("delay_trigger_block.triggerDelay.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			delayTriggerBlockEntity.markDirty();
			world.updateListeners(delayTriggerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
