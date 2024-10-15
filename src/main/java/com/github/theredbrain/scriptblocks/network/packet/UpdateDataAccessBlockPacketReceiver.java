package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateDataAccessBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateDataAccessBlockPacket> {
	@Override
	public void receive(UpdateDataAccessBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos dataAccessBlockPosition = payload.dataAccessBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPositionOffset();
		BlockPos firstTriggeredBlockPositionOffset = payload.firstTriggeredBlockPositionOffset();
		boolean firstTriggeredBlockResets = payload.firstTriggeredBlockResets();
		BlockPos secondTriggeredBlockPositionOffset = payload.secondTriggeredBlockPositionOffset();
		boolean secondTriggeredBlockResets = payload.secondTriggeredBlockResets();
		boolean isWriting = payload.isWriting();
		String dataIdentifier = payload.dataIdentifier();
		int comparedDataValue = payload.comparedDataValue();
		DataAccessBlockEntity.DataReadingMode dataReadingMode = DataAccessBlockEntity.DataReadingMode.byName(payload.dataReadingMode()).orElse(DataAccessBlockEntity.DataReadingMode.LESSER);
		boolean isAdding = payload.isAdding();
		int newDataValue = payload.newDataValue();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(dataAccessBlockPosition);
		BlockState blockState = world.getBlockState(dataAccessBlockPosition);

		if (blockEntity instanceof DataAccessBlockEntity dataAccessBlockEntity) {
			dataAccessBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			dataAccessBlockEntity.setFirstTriggeredBlock(new MutablePair<>(firstTriggeredBlockPositionOffset, firstTriggeredBlockResets));
			dataAccessBlockEntity.setSecondTriggeredBlock(new MutablePair<>(secondTriggeredBlockPositionOffset, secondTriggeredBlockResets));
			dataAccessBlockEntity.setIsWriting(isWriting);
			dataAccessBlockEntity.setDataIdentifier(dataIdentifier);
			dataAccessBlockEntity.setComparedDataValue(comparedDataValue);
			dataAccessBlockEntity.setDataReadingMode(dataReadingMode);
			dataAccessBlockEntity.setIsAdding(isAdding);
			dataAccessBlockEntity.setNewDataValue(newDataValue);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			dataAccessBlockEntity.markDirty();
			world.updateListeners(dataAccessBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
