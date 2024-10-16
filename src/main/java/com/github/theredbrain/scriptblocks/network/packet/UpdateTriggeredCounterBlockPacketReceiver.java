package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;

public class UpdateTriggeredCounterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredCounterBlockPacket> {

	@Override
	public void receive(UpdateTriggeredCounterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos triggeredCounterBlockPosition = payload.triggeredCounterBlockPosition();

		List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList = payload.triggeredBlocksList();
		HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
		for (MutablePair<Integer, MutablePair<BlockPos, Boolean>> usedBlock : triggeredBlocksList) {
			triggeredBlocks.put(usedBlock.getLeft(), usedBlock.getRight());
		}

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredCounterBlockPosition);
		BlockState blockState = world.getBlockState(triggeredCounterBlockPosition);

		if (blockEntity instanceof TriggeredCounterBlockEntity triggeredCounterBlockEntity) {
			if (!triggeredCounterBlockEntity.setTriggeredBlocks(triggeredBlocks)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_block.triggeredBlocks.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredCounterBlockEntity.markDirty();
			world.updateListeners(triggeredCounterBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
