package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;

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

		List<LevelData.RespawnData> delegatedSpawnPoints = payload.delegatedSpawnPoints();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(spawnPointDelegationBlockPosition);
		BlockState blockState = serverLevel.getBlockState(spawnPointDelegationBlockPosition);

		if (blockEntity instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			spawnPointDelegationBlockEntity.setDelegatedSpawnPoints(delegatedSpawnPoints);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			spawnPointDelegationBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(spawnPointDelegationBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
