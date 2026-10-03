package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
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

public class UpdatePlayerDetectorBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdatePlayerDetectorBlockPacket> {
	@Override
	public void receive(UpdatePlayerDetectorBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
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

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(areaBlockPosition);
		BlockState blockState = world.getBlockState(areaBlockPosition);

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

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			playerDetectorBlockEntity.markDirty();
			world.updateListeners(areaBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
