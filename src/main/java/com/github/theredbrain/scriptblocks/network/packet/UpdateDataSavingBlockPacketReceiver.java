package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateDataSavingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataSavingBlockPacket> {
	@Override
	public void receive(UpdateDataSavingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos dataSavingBlockPosition = payload.dataSavingBlockPosition();

		List<MutablePair<String, String>> dataList = payload.dataList();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataSavingBlockPosition);
		BlockState blockState = world.getBlockState(dataSavingBlockPosition);

		if (blockEntity instanceof DataSavingBlockEntity dataSavingBlockEntity) {
			dataSavingBlockEntity.setDataList(dataList);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dataSavingBlockEntity.markDirty();
			world.updateListeners(dataSavingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
