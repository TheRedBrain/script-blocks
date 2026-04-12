package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateSpawnPointDelegationBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateSpawnPointDelegationBlockPacket> {
	@Override
	public void receive(UpdateSpawnPointDelegationBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos entranceDelegationBlockPosition = payload.entranceDelegationBlockPosition();

		BlockPos delegatedEntrancePositionOffset = payload.delegatedEntrancePositionOffset();
		double delegatedEntranceYaw = payload.delegatedEntranceYaw();
		double delegatedEntrancePitch = payload.delegatedEntrancePitch();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(entranceDelegationBlockPosition);
		BlockState blockState = world.getBlockState(entranceDelegationBlockPosition);

		if (blockEntity instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			if (!spawnPointDelegationBlockEntity.setDelegatedSpawnPoint(new MutablePair<>(delegatedEntrancePositionOffset, new MutablePair<>(delegatedEntranceYaw, delegatedEntrancePitch)))) {
				serverPlayerEntity.sendMessage(Text.translatable("entrance_delegation_block.delegatedEntrance.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			spawnPointDelegationBlockEntity.markDirty();
			world.updateListeners(entranceDelegationBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
