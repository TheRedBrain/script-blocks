package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateSpawnPointDelegationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateSpawnPointDelegationBlockPacket> {
	@Override
	public void receive(UpdateSpawnPointDelegationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos spawnPointDelegationBlockPosition = payload.spawnPointDelegationBlockPosition();

		List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints = payload.delegatedSpawnPoints();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(spawnPointDelegationBlockPosition);
		BlockState blockState = world.getBlockState(spawnPointDelegationBlockPosition);

		if (blockEntity instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			spawnPointDelegationBlockEntity.setDelegatedSpawnPoints(delegatedSpawnPoints);
			spawnPointDelegationBlockEntity.markDirty();
			world.updateListeners(spawnPointDelegationBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
