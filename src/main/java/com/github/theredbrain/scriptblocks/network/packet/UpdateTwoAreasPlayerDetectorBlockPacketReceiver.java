package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTwoAreasPlayerDetectorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTwoAreasPlayerDetectorBlockPacket> {
	@Override
	public void receive(UpdateTwoAreasPlayerDetectorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
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

		World world = serverPlayerEntity.getWorld();

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

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			twoAreasPlayerDetectorBlock.markDirty();
			world.updateListeners(twoAreasPlayerDetectorBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
