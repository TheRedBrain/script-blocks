package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UpdateTriggeredDisplayBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredDisplayBlockPacket> {
	@Override
	public void receive(UpdateTriggeredDisplayBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredDisplayBlockPosition = payload.triggeredDisplayBlockPosition();

		// common
		TriggeredDisplayBlockEntity.BillboardConstraints billboardMode = TriggeredDisplayBlockEntity.BillboardConstraints.byName(payload.billboardModeString()).orElse(TriggeredDisplayBlockEntity.BillboardConstraints.FIXED);
		TriggeredDisplayBlockEntity.DisplayMode displayMode = TriggeredDisplayBlockEntity.DisplayMode.byName(payload.displayModeString()).orElse(TriggeredDisplayBlockEntity.DisplayMode.TEXT);
		boolean isTriggered = payload.isTriggered();
		Vec3 displayOffset = payload.displayOffset();
		float displayYaw = payload.displayYaw();
		float displayPitch = payload.displayPitch();
		// text mode
		String displayTextString = payload.displayTextString();
		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPosOffset();
		String dataIdentifierString = payload.dataIdentifierString();
		int lineWidth = payload.lineWidth();
		Byte textOpacity = payload.textOpacity();
		int textBackground = payload.textBackground();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(triggeredDisplayBlockPosition);
		BlockState blockState = serverLevel.getBlockState(triggeredDisplayBlockPosition);

		if (blockEntity instanceof TriggeredDisplayBlockEntity triggeredDisplayBlockEntity) {
			triggeredDisplayBlockEntity.reset();
			triggeredDisplayBlockEntity.setBillboardConstraints(billboardMode);
			triggeredDisplayBlockEntity.setDisplayMode(displayMode);
			triggeredDisplayBlockEntity.setIsTriggered(isTriggered);
			triggeredDisplayBlockEntity.setDisplayOffset(displayOffset);
			triggeredDisplayBlockEntity.setDisplayRotation(displayYaw, displayPitch);
			triggeredDisplayBlockEntity.setTextString(displayTextString);
			triggeredDisplayBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			triggeredDisplayBlockEntity.setDataIdentifierString(dataIdentifierString);
			triggeredDisplayBlockEntity.setLineWidth(lineWidth);
			triggeredDisplayBlockEntity.setTextOpacity(textOpacity);
			triggeredDisplayBlockEntity.setBackground(textBackground);
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			triggeredDisplayBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(triggeredDisplayBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
