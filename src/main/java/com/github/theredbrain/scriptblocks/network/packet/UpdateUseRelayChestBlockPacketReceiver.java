package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateUseRelayChestBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateUseRelayChestBlockPacket> {
	@Override
	public void receive(UpdateUseRelayChestBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(useRelayChestBlockPosition);
		BlockState blockState = world.getBlockState(useRelayChestBlockPosition);

		if (blockEntity instanceof UseRelayChestBlockEntity useRelayChestBlockEntity) {
			useRelayChestBlockEntity.reset();
			useRelayChestBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			useRelayChestBlockEntity.setKeyIdentifierString(keyItemTag);
			useRelayChestBlockEntity.setLockedMessage(lockedMessage);
			useRelayChestBlockEntity.setLockedSound(lockedSound);
			useRelayChestBlockEntity.setUnlockedMessage(unlockedMessage);
			useRelayChestBlockEntity.setUnlockedSound(unlockedSound);
			useRelayChestBlockEntity.setRelayBlockPositionOffset(relayBlockPositionOffset);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			useRelayChestBlockEntity.markDirty();
			world.updateListeners(useRelayChestBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
