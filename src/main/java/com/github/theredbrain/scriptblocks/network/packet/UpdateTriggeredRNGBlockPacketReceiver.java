package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class UpdateTriggeredRNGBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredRNGBlockPacket> {
	@Override
	public void receive(UpdateTriggeredRNGBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		World world = serverPlayerEntity.getWorld();

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
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			triggeredRNGBlockEntity.markDirty();
			world.updateListeners(triggeredRNGBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
