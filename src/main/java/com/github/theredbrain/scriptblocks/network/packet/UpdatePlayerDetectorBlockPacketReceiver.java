package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdatePlayerDetectorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdatePlayerDetectorBlockPacket> {
	@Override
	public void receive(UpdatePlayerDetectorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		boolean enableTicking = payload.enableTicking();

		BlockPos areaBlockPosition = payload.areaBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		BlockPos onEnteringTriggeredBlockPositionOffset = payload.onEnteringTriggeredBlockPositionOffset();
		boolean onEnteringTriggeredBlockResets = payload.onEnteringTriggeredBlockResets();

		BlockPos onLeavingTriggeredBlockPositionOffset = payload.onLeavingTriggeredBlockPositionOffset();
		boolean onLeavingTriggeredBlockResets = payload.onLeavingTriggeredBlockResets();

		BlockPos onTriggeringTriggeredBlockPositionOffset = payload.onTriggeringTriggeredBlockPositionOffset();
		boolean onTriggeringTriggeredBlockResets = payload.onTriggeringTriggeredBlockResets();

		BlockPos onEnteringUUIDListHandler = payload.onEnteringUUIDListHandler();
		BlockPos onLeavingUUIDListHandler = payload.onLeavingUUIDListHandler();
		BlockPos onTriggeringUUIDListHandler = payload.onTriggeringUUIDListHandler();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(areaBlockPosition);
		BlockState blockState = serverLevel.getBlockState(areaBlockPosition);

		if (blockEntity instanceof PlayerDetectorBlockEntity playerDetectorBlockEntity) {
			playerDetectorBlockEntity.setEnableTicking(enableTicking);
			playerDetectorBlockEntity.setShowArea(showArea);
			playerDetectorBlockEntity.setAreaDimensions(areaDimensions);
			playerDetectorBlockEntity.setAreaPositionOffset(areaPositionOffset);
			playerDetectorBlockEntity.setOnEnteringTriggeredBlock(new MutablePair<>(onEnteringTriggeredBlockPositionOffset, onEnteringTriggeredBlockResets));
			playerDetectorBlockEntity.setOnLeavingTriggeredBlock(new MutablePair<>(onLeavingTriggeredBlockPositionOffset, onLeavingTriggeredBlockResets));
			playerDetectorBlockEntity.setOnTriggeringTriggeredBlock(new MutablePair<>(onTriggeringTriggeredBlockPositionOffset, onTriggeringTriggeredBlockResets));
			playerDetectorBlockEntity.setOnEnteringUUIDListHandler(onEnteringUUIDListHandler);
			playerDetectorBlockEntity.setOnLeavingUUIDListHandler(onLeavingUUIDListHandler);
			playerDetectorBlockEntity.setOnTriggeringUUIDListHandler(onTriggeringUUIDListHandler);

			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			playerDetectorBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(areaBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
