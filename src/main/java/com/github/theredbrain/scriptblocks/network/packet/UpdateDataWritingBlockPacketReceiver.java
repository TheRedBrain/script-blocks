package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateDataWritingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataWritingBlockPacket> {
	@Override
	public void receive(UpdateDataWritingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos dataAccessBlockPosition = payload.dataAccessBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPositionOffset();
		String dataIdentifier = payload.dataIdentifier();
		String newDataValue = payload.newDataValue();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataAccessBlockPosition);
		BlockState blockState = world.getBlockState(dataAccessBlockPosition);

		if (blockEntity instanceof DataWritingBlockEntity dataWritingBlockEntity) {
			dataWritingBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			dataWritingBlockEntity.setDataIdentifier(dataIdentifier);
			dataWritingBlockEntity.setNewDataValue(newDataValue);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			dataWritingBlockEntity.setChanged();
			world.sendBlockUpdated(dataAccessBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
