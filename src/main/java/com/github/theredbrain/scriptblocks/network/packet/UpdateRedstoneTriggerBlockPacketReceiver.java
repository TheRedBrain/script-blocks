package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateRedstoneTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateRedstoneTriggerBlockPacket> {
	@Override
	public void receive(UpdateRedstoneTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos redstoneTriggerBlockPosition = payload.redstoneTriggerBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(redstoneTriggerBlockPosition);
		BlockState blockState = world.getBlockState(redstoneTriggerBlockPosition);

		if (blockEntity instanceof RedstoneTriggerBlockEntity redstoneTriggerBlockEntity) {
			redstoneTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			redstoneTriggerBlockEntity.setChanged();
			world.sendBlockUpdated(redstoneTriggerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
