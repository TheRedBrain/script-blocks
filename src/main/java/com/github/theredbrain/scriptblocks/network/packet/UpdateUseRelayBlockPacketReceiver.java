package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateUseRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUseRelayBlockPacket> {
	@Override
	public void receive(UpdateUseRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos useRelayBlockPosition = payload.useRelayBlockPosition();

		BlockPos relayBlockPositionOffset = payload.relayBlockPositionOffset();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(useRelayBlockPosition);
		BlockState blockState = world.getBlockState(useRelayBlockPosition);

		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			useRelayBlockEntity.setRelayBlockPositionOffset(relayBlockPositionOffset);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			useRelayBlockEntity.setChanged();
			world.sendBlockUpdated(useRelayBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
