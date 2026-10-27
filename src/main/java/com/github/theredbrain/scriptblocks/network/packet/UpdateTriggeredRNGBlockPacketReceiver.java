package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class UpdateTriggeredRNGBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredRNGBlockPacket> {
	@Override
	public void receive(UpdateTriggeredRNGBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredRNGBlockPosition = payload.triggeredRNGBlockPosition();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPosOffset();
		String overrideDataIdentifier = payload.overrideDataIdentifier();
		String overrideDataValue = payload.overrideDataValue();
		BlockPos overrideTriggeredBlockPosOffset = payload.overrideTriggeredBlockPosOffset();
		boolean overrideTriggeredBlockResets = payload.overrideTriggeredBlockResets();
		String influencingAttributeIdentifierString = payload.influencingAttributeIdentifierString();
		boolean checksTeamAttributes = payload.checksTeamAttributes();
		boolean isAffectedByLuck = payload.isAffectedByLuck();
		int randomMinValue = payload.randomMinValue();
		int randomMaxValue = payload.randomMaxValue();
		BlockPos fallbackTriggeredBlockPosOffset = payload.fallbackTriggeredBlockPosOffset();
		boolean fallbackTriggeredBlockResets = payload.fallbackTriggeredBlockResets();
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks = new ArrayList<>(payload.triggeredBlocks());

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeredRNGBlockPosition);
		BlockState blockState = world.getBlockState(triggeredRNGBlockPosition);

		if (blockEntity instanceof TriggeredRNGBlockEntity triggeredRNGBlockEntity) {
			triggeredRNGBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			triggeredRNGBlockEntity.setOverrideDataIdentifier(overrideDataIdentifier);
			triggeredRNGBlockEntity.setOverrideDataValue(overrideDataValue);
			triggeredRNGBlockEntity.setOverrideTriggeredBlock(new MutablePair<>(overrideTriggeredBlockPosOffset, overrideTriggeredBlockResets));
			triggeredRNGBlockEntity.setInfluencingAttributeIdentifierString(influencingAttributeIdentifierString);
			triggeredRNGBlockEntity.setChecksTeamAttributes(checksTeamAttributes);
			triggeredRNGBlockEntity.setIsAffectedByLuck(isAffectedByLuck);
			triggeredRNGBlockEntity.setRandomMinValue(randomMinValue);
			triggeredRNGBlockEntity.setRandomMaxValue(randomMaxValue);
			triggeredRNGBlockEntity.setFallbackTriggeredBlock(new MutablePair<>(fallbackTriggeredBlockPosOffset, fallbackTriggeredBlockResets));
			triggeredRNGBlockEntity.setTriggeredBlocks(triggeredBlocks);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			triggeredRNGBlockEntity.setChanged();
			world.sendBlockUpdated(triggeredRNGBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
