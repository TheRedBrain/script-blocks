package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateRedstoneTriggerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateRedstoneTriggerBlockPacket> {
	@Override
	public void receive(UpdateRedstoneTriggerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos redstoneTriggerBlockPosition = payload.redstoneTriggerBlockPosition();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();

		boolean triggeredBlockResets = payload.triggeredBlockResets();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(redstoneTriggerBlockPosition);
		BlockState blockState = world.getBlockState(redstoneTriggerBlockPosition);

		if (blockEntity instanceof RedstoneTriggerBlockEntity redstoneTriggerBlockEntity) {
			redstoneTriggerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			redstoneTriggerBlockEntity.markDirty();
			world.updateListeners(redstoneTriggerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
