package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTwoAreasPlayerDetectorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTwoAreasPlayerDetectorBlockPacket> {
	@Override
	public void receive(UpdateTwoAreasPlayerDetectorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos twoAreasPlayerDetectorBlockPosition = payload.twoAreasPlayerDetectorBlockPosition();

		boolean showFirstArea = payload.showFirstArea();
		boolean showSecondArea = payload.showSecondArea();

		Vec3i firstAreaDimensions = payload.firstAreaDimensions();
		BlockPos firstAreaPositionOffset = payload.firstAreaPositionOffset();

		Vec3i secondAreaDimensions = payload.secondAreaDimensions();
		BlockPos secondAreaPositionOffset = payload.secondAreaPositionOffset();

		BlockPos onEnteringFirstAreaTriggeredBlockPositionOffset = payload.onEnteringFirstAreaTriggeredBlockPositionOffset();
		boolean onEnteringFirstAreaTriggeredBlockResets = payload.onEnteringFirstAreaTriggeredBlockResets();

		BlockPos onEnteringSecondAreaTriggeredBlockPositionOffset = payload.onEnteringSecondAreaTriggeredBlockPositionOffset();
		boolean onEnteringSecondAreaTriggeredBlockResets = payload.onEnteringSecondAreaTriggeredBlockResets();

		BlockPos onEnteringFirstAreaUUIDListHandler = payload.onEnteringFirstAreaUUIDListHandler();
		BlockPos onEnteringSecondAreaUUIDListHandler = payload.onEnteringSecondAreaUUIDListHandler();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(twoAreasPlayerDetectorBlockPosition);
		BlockState blockState = world.getBlockState(twoAreasPlayerDetectorBlockPosition);

		if (blockEntity instanceof TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlock) {
			twoAreasPlayerDetectorBlock.reset();
			twoAreasPlayerDetectorBlock.setShowFirstArea(showFirstArea);
			twoAreasPlayerDetectorBlock.setShowSecondArea(showSecondArea);
			twoAreasPlayerDetectorBlock.setFirstAreaDimensions(firstAreaDimensions);
			twoAreasPlayerDetectorBlock.setFirstAreaPositionOffset(firstAreaPositionOffset);
			twoAreasPlayerDetectorBlock.setSecondAreaDimensions(secondAreaDimensions);
			twoAreasPlayerDetectorBlock.setSecondAreaPositionOffset(secondAreaPositionOffset);
			twoAreasPlayerDetectorBlock.setOnEnteringFirstAreaTriggeredBlock(new MutablePair<>(onEnteringFirstAreaTriggeredBlockPositionOffset, onEnteringFirstAreaTriggeredBlockResets));
			twoAreasPlayerDetectorBlock.setOnEnteringSecondAreaTriggeredBlock(new MutablePair<>(onEnteringSecondAreaTriggeredBlockPositionOffset, onEnteringSecondAreaTriggeredBlockResets));
			twoAreasPlayerDetectorBlock.setOnEnteringFirstAreaUUIDListHandler(onEnteringFirstAreaUUIDListHandler);
			twoAreasPlayerDetectorBlock.setOnEnteringSecondAreaUUIDListHandler(onEnteringSecondAreaUUIDListHandler);

			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			twoAreasPlayerDetectorBlock.setChanged();
			world.sendBlockUpdated(twoAreasPlayerDetectorBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
