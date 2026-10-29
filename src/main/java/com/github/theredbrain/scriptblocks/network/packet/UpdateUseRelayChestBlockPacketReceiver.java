package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateUseRelayChestBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUseRelayChestBlockPacket> {
	@Override
	public void receive(UpdateUseRelayChestBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos useRelayChestBlockPosition = payload.useRelayChestBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		String keyItemTag = payload.keyItemTag();

		String lockedMessage = payload.lockedMessage();

		String lockedSound = payload.lockedSound();

		String unlockedMessage = payload.unlockedMessage();

		String unlockedSound = payload.unlockedSound();

		BlockPos relayBlockPositionOffset = payload.relayBlockPositionOffset();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(useRelayChestBlockPosition);
		BlockState blockState = serverLevel.getBlockState(useRelayChestBlockPosition);

		if (blockEntity instanceof UseRelayChestBlockEntity useRelayChestBlockEntity) {
			useRelayChestBlockEntity.reset();
			useRelayChestBlockEntity.setTriggeredBlock(new MutablePair<>(new BlockPos(triggeredBlockPositionOffset), triggeredBlockResets));
			useRelayChestBlockEntity.setKeyIdentifierString(keyItemTag);
			useRelayChestBlockEntity.setLockedMessage(lockedMessage);
			useRelayChestBlockEntity.setLockedSound(lockedSound);
			useRelayChestBlockEntity.setUnlockedMessage(unlockedMessage);
			useRelayChestBlockEntity.setUnlockedSound(unlockedSound);
			useRelayChestBlockEntity.setRelayBlockPositionOffset(relayBlockPositionOffset);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			useRelayChestBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(useRelayChestBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
