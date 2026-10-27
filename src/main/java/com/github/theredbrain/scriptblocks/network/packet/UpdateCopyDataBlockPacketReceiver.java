package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateCopyDataBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateCopyDataBlockPacket> {
	@Override
	public void receive(UpdateCopyDataBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos copyDataBlockPosition = payload.copyDataBlockPosition();

		BlockPos firstDataProvidingBlockPosOffset = payload.firstDataProvidingBlockPositionOffset();
		BlockPos secondDataProvidingBlockPosOffset = payload.secondDataProvidingBlockPositionOffset();
		String dataIdentifier = payload.dataIdentifier();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(copyDataBlockPosition);
		BlockState blockState = world.getBlockState(copyDataBlockPosition);

		if (blockEntity instanceof CopyDataBlockEntity copyDataBlockEntity) {
			copyDataBlockEntity.setFirstDataProvidingBlockPosOffset(firstDataProvidingBlockPosOffset);
			copyDataBlockEntity.setSecondDataProvidingBlockPosOffset(secondDataProvidingBlockPosOffset);
			copyDataBlockEntity.setDataIdentifier(dataIdentifier);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			copyDataBlockEntity.setChanged();
			world.sendBlockUpdated(copyDataBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
