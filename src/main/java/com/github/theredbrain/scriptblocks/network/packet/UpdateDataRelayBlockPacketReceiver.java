package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;

public class UpdateDataRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataRelayBlockPacket> {
	@Override
	public void receive(UpdateDataRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos dataRelayBlockPosition = payload.dataRelayBlockPosition();

		List<BlockPos> dataProvidingBlockPosOffsetList = payload.dataProvidingBlockPosOffsetList();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataRelayBlockPosition);
		BlockState blockState = world.getBlockState(dataRelayBlockPosition);

		if (blockEntity instanceof DataRelayBlockEntity dataRelayBlockEntity) {
			dataRelayBlockEntity.reset();
			dataRelayBlockEntity.setDataProvidingBlockPosOffsetList(dataProvidingBlockPosOffsetList);
			dataRelayBlockEntity.setIndex(0);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			dataRelayBlockEntity.setChanged();
			world.sendBlockUpdated(dataRelayBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
