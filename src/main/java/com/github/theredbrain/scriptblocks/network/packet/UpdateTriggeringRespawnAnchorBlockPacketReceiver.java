package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeringRespawnAnchorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeringRespawnAnchorBlockPacket> {
	@Override
	public void receive(UpdateTriggeringRespawnAnchorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeringRespawnAnchorBlockPosition = payload.triggeringRespawnAnchorBlockPosition();

		BlockPos spawnPointPositionOffset = payload.spawnPointPositionOffset();
		double spawnPointYaw = payload.spawnPointYaw();
		double spawnPointPitch = payload.spawnPointPitch();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		boolean sendMessage = payload.sendMessage();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeringRespawnAnchorBlockPosition);
		BlockState blockState = world.getBlockState(triggeringRespawnAnchorBlockPosition);

		if (blockEntity instanceof TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
			triggeringRespawnAnchorBlockEntity.reset();
			triggeringRespawnAnchorBlockEntity.setSpawnPoint(new MutablePair<>(new BlockPos(spawnPointPositionOffset), new MutablePair<>(spawnPointYaw, spawnPointPitch)));
			triggeringRespawnAnchorBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			triggeringRespawnAnchorBlockEntity.setSendMessage(sendMessage);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			triggeringRespawnAnchorBlockEntity.markDirty();
			world.updateListeners(triggeringRespawnAnchorBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
