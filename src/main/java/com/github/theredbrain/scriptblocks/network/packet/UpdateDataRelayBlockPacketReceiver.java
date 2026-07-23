package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class UpdateDataRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataRelayBlockPacket> {
	@Override
	public void receive(UpdateDataRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos dataRelayBlockPosition = payload.dataRelayBlockPosition();

		List<BlockPos> dataProvidingBlockPosOffsetList = payload.dataProvidingBlockPosOffsetList();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataRelayBlockPosition);
		BlockState blockState = world.getBlockState(dataRelayBlockPosition);

		if (blockEntity instanceof DataRelayBlockEntity dataRelayBlockEntity) {
//			dataRelayBlockEntity.reset(); // TODO test if the coordinate sign flipping stops
			dataRelayBlockEntity.setDataProvidingBlockPosOffsetList(dataProvidingBlockPosOffsetList);
			dataRelayBlockEntity.setIndex(0);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dataRelayBlockEntity.markDirty();
			world.updateListeners(dataRelayBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
