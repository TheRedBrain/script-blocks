package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateInteractiveTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateInteractiveTriggerBlockPacket> {
	@Override
	public void receive(UpdateInteractiveTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos interactiveTriggerBlockPosition = payload.interactiveTriggerBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		String preventInteractionIdentifierString = payload.preventInteractionIdentifierString();

		String keyIdentifierString = payload.keyIdentifierString();

		String interactionPreventedMessage = payload.interactionPreventedMessage();

		String interactionPreventedSound = payload.interactionPreventedSound();

		String lockedMessage = payload.lockedMessage();

		String lockedSound = payload.lockedSound();

		String unlockedMessage = payload.unlockedMessage();

		String unlockedSound = payload.unlockedSound();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(interactiveTriggerBlockPosition);
		BlockState blockState = world.getBlockState(interactiveTriggerBlockPosition);

		if (blockEntity instanceof InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
			interactiveTriggerBlockEntity.reset();
			interactiveTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			interactiveTriggerBlockEntity.setPreventInteractionIdentifierString(preventInteractionIdentifierString);
			interactiveTriggerBlockEntity.setKeyIdentifierString(keyIdentifierString);
			interactiveTriggerBlockEntity.setInteractionPreventedMessage(interactionPreventedMessage);
			interactiveTriggerBlockEntity.setInteractionPreventedSound(interactionPreventedSound);
			interactiveTriggerBlockEntity.setLockedMessage(lockedMessage);
			interactiveTriggerBlockEntity.setLockedSound(lockedSound);
			interactiveTriggerBlockEntity.setUnlockedMessage(unlockedMessage);
			interactiveTriggerBlockEntity.setUnlockedSound(unlockedSound);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			interactiveTriggerBlockEntity.setChanged();
			world.sendBlockUpdated(interactiveTriggerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
