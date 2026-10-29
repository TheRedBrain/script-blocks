package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateDataSavingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataSavingBlockPacket> {
	@Override
	public void receive(UpdateDataSavingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos dataSavingBlockPosition = payload.dataSavingBlockPosition();

		List<MutablePair<String, String>> dataList = payload.dataList();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(dataSavingBlockPosition);
		BlockState blockState = serverLevel.getBlockState(dataSavingBlockPosition);

		if (blockEntity instanceof DataSavingBlockEntity dataSavingBlockEntity) {
			dataSavingBlockEntity.reset();
			dataSavingBlockEntity.setDataList(dataList);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			dataSavingBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(dataSavingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
