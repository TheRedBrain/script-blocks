package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateCopyDataBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateCopyDataBlockPacket> {
	@Override
	public void receive(UpdateCopyDataBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos copyDataBlockPosition = payload.copyDataBlockPosition();

		BlockPos firstDataProvidingBlockPosOffset = payload.firstDataProvidingBlockPositionOffset();
		BlockPos secondDataProvidingBlockPosOffset = payload.secondDataProvidingBlockPositionOffset();
		String dataIdentifier = payload.dataIdentifier();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(copyDataBlockPosition);
		BlockState blockState = world.getBlockState(copyDataBlockPosition);

		if (blockEntity instanceof CopyDataBlockEntity copyDataBlockEntity) {
			copyDataBlockEntity.setFirstDataProvidingBlockPosOffset(firstDataProvidingBlockPosOffset);
			copyDataBlockEntity.setSecondDataProvidingBlockPosOffset(secondDataProvidingBlockPosOffset);
			copyDataBlockEntity.setDataIdentifier(dataIdentifier);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			copyDataBlockEntity.markDirty();
			world.updateListeners(copyDataBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
