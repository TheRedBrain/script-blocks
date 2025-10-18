package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateInteractiveTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateInteractiveTriggerBlockPacket> {
	@Override
	public void receive(UpdateInteractiveTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos interactiveTriggerBlockPosition = payload.interactiveTriggerBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		String keyItemTag = payload.keyItemTag();

		String lockedMessage = payload.lockedMessage();

		String lockedSound = payload.lockedSound();

		String unlockedMessage = payload.unlockedMessage();

		String unlockedSound = payload.unlockedSound();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(interactiveTriggerBlockPosition);
		BlockState blockState = world.getBlockState(interactiveTriggerBlockPosition);

		if (blockEntity instanceof InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
			interactiveTriggerBlockEntity.reset();
			interactiveTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			interactiveTriggerBlockEntity.setKeyIdentifierString(keyItemTag);
			interactiveTriggerBlockEntity.setLockedMessage(lockedMessage);
			interactiveTriggerBlockEntity.setLockedSound(lockedSound);
			interactiveTriggerBlockEntity.setUnlockedMessage(unlockedMessage);
			interactiveTriggerBlockEntity.setUnlockedSound(unlockedSound);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			interactiveTriggerBlockEntity.markDirty();
			world.updateListeners(interactiveTriggerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
