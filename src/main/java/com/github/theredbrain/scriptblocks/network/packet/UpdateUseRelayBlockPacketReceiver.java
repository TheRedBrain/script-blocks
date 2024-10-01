package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateUseRelayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUseRelayBlockPacket> {
	@Override
	public void receive(UpdateUseRelayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos useRelayBlockPosition = payload.useRelayBlockPosition();

		BlockPos relayBlockPositionOffset = payload.relayBlockPositionOffset();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(useRelayBlockPosition);
		BlockState blockState = world.getBlockState(useRelayBlockPosition);

		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			if (!useRelayBlockEntity.setRelayBlockPositionOffset(relayBlockPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("use_relay_block.relayBlockPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			useRelayBlockEntity.markDirty();
			world.updateListeners(useRelayBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
