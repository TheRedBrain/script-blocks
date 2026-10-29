package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;

public class UpdateTriggeredCounterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredCounterBlockPacket> {

	@Override
	public void receive(UpdateTriggeredCounterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredCounterBlockPosition = payload.triggeredCounterBlockPosition();

		List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList = payload.triggeredBlocksList();
		HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
		for (MutablePair<Integer, MutablePair<BlockPos, Boolean>> usedBlock : triggeredBlocksList) {
			triggeredBlocks.put(usedBlock.getLeft(), usedBlock.getRight());
		}

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(triggeredCounterBlockPosition);
		BlockState blockState = serverLevel.getBlockState(triggeredCounterBlockPosition);

		if (blockEntity instanceof TriggeredCounterBlockEntity triggeredCounterBlockEntity) {
			triggeredCounterBlockEntity.reset();
			if (!triggeredCounterBlockEntity.setTriggeredBlocks(triggeredBlocks)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_block.triggeredBlocks.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredCounterBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(triggeredCounterBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
