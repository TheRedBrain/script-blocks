package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateDataModificationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataModificationBlockPacket> {
	@Override
	public void receive(UpdateDataModificationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos dataAccessBlockPosition = payload.dataAccessBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPositionOffset();
		DataModificationBlockEntity.DataModificationMode dataModificationMode = DataModificationBlockEntity.DataModificationMode.byName(payload.dataModificationMode()).orElse(DataModificationBlockEntity.DataModificationMode.INTEGER_ADDITION);
		int addedIntegerValue = payload.addedIntegerValue();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataAccessBlockPosition);
		BlockState blockState = world.getBlockState(dataAccessBlockPosition);

		if (blockEntity instanceof DataModificationBlockEntity dataModificationBlockEntity) {
			dataModificationBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			dataModificationBlockEntity.setDataModificationMode(dataModificationMode);
			dataModificationBlockEntity.setAddedIntegerValue(addedIntegerValue);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dataModificationBlockEntity.markDirty();
			world.updateListeners(dataAccessBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
