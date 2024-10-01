package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetManualResetLocationControlBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<SetManualResetLocationControlBlockPacket> {
	@Override
	public void receive(SetManualResetLocationControlBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos locationControlBlockPosition = payload.locationControlBlockPosition();

		boolean manualReset = payload.manualReset();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(locationControlBlockPosition);
		BlockState blockState = world.getBlockState(locationControlBlockPosition);

		if (blockEntity instanceof LocationControlBlockEntity locationControlBlockEntity) {

			locationControlBlockEntity.setManualReset(manualReset);

			locationControlBlockEntity.markDirty();
			world.updateListeners(locationControlBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
