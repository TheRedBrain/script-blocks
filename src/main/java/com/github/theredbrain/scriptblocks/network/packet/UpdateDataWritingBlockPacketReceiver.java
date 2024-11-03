package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateDataWritingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataWritingBlockPacket> {
	@Override
	public void receive(UpdateDataWritingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos dataAccessBlockPosition = payload.dataAccessBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPositionOffset();
		String dataIdentifier = payload.dataIdentifier();
		String newDataValue = payload.newDataValue();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataAccessBlockPosition);
		BlockState blockState = world.getBlockState(dataAccessBlockPosition);

		if (blockEntity instanceof DataWritingBlockEntity dataWritingBlockEntity) {
			dataWritingBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			dataWritingBlockEntity.setDataIdentifier(dataIdentifier);
			dataWritingBlockEntity.setNewDataValue(newDataValue);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dataWritingBlockEntity.markDirty();
			world.updateListeners(dataAccessBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
