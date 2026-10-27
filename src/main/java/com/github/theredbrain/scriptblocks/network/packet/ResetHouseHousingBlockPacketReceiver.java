package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ResetHouseHousingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<ResetHouseHousingBlockPacket> {
	@Override
	public void receive(ResetHouseHousingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {
			housingBlockEntity.trigger();
		}
	}
}
