package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateDataModificationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataModificationBlockPacket> {
	@Override
	public void receive(UpdateDataModificationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos dataAccessBlockPosition = payload.dataAccessBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPositionOffset();
		DataModificationBlockEntity.DataModificationMode dataModificationMode = DataModificationBlockEntity.DataModificationMode.byName(payload.dataModificationMode()).orElse(DataModificationBlockEntity.DataModificationMode.INTEGER_ADDITION);
		int addedIntegerValue = payload.addedIntegerValue();
		int integerModulusValue = payload.integerModulusValue();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataAccessBlockPosition);
		BlockState blockState = world.getBlockState(dataAccessBlockPosition);

		if (blockEntity instanceof DataModificationBlockEntity dataModificationBlockEntity) {
			dataModificationBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			dataModificationBlockEntity.setDataModificationMode(dataModificationMode);
			dataModificationBlockEntity.setAddedIntegerValue(addedIntegerValue);
			dataModificationBlockEntity.setIntegerModulusValue(integerModulusValue);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			dataModificationBlockEntity.setChanged();
			world.sendBlockUpdated(dataAccessBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
