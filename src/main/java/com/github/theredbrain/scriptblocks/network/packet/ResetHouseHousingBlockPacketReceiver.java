package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ResetHouseHousingBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<ResetHouseHousingBlockPacket> {
	@Override
	public void receive(ResetHouseHousingBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);

		// TODO teleport all serverPlayerEntitys inside to their spawn?
		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {
			housingBlockEntity.trigger();
		}
	}
}
