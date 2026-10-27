package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateSpawnPointDelegationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateSpawnPointDelegationBlockPacket> {
	@Override
	public void receive(UpdateSpawnPointDelegationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos spawnPointDelegationBlockPosition = payload.spawnPointDelegationBlockPosition();

		List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints = payload.delegatedSpawnPoints();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(spawnPointDelegationBlockPosition);
		BlockState blockState = world.getBlockState(spawnPointDelegationBlockPosition);

		if (blockEntity instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			spawnPointDelegationBlockEntity.setDelegatedSpawnPoints(delegatedSpawnPoints);
			spawnPointDelegationBlockEntity.setChanged();
			world.sendBlockUpdated(spawnPointDelegationBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
