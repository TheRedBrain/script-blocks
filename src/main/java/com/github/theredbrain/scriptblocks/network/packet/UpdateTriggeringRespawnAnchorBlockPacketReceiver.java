package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeringRespawnAnchorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeringRespawnAnchorBlockPacket> {
	@Override
	public void receive(UpdateTriggeringRespawnAnchorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeringRespawnAnchorBlockPosition = payload.triggeringRespawnAnchorBlockPosition();

		BlockPos spawnPointPositionOffset = payload.spawnPointPositionOffset();
		double spawnPointYaw = payload.spawnPointYaw();
		double spawnPointPitch = payload.spawnPointPitch();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		boolean sendMessage = payload.sendMessage();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeringRespawnAnchorBlockPosition);
		BlockState blockState = world.getBlockState(triggeringRespawnAnchorBlockPosition);

		if (blockEntity instanceof TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
			triggeringRespawnAnchorBlockEntity.reset();
			triggeringRespawnAnchorBlockEntity.setSpawnPoint(new MutablePair<>(new BlockPos(spawnPointPositionOffset), new MutablePair<>(spawnPointYaw, spawnPointPitch)));
			triggeringRespawnAnchorBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			triggeringRespawnAnchorBlockEntity.setSendMessage(sendMessage);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			triggeringRespawnAnchorBlockEntity.setChanged();
			world.sendBlockUpdated(triggeringRespawnAnchorBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
