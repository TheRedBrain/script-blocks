package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateAreaFillerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateAreaFillerBlockPacket> {
	@Override
	public void receive(UpdateAreaFillerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos areaFillerBlockPosition = payload.areaFillerBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		String blockIdentifier = payload.blockIdentifier();

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(areaFillerBlockPosition);
		BlockState blockState = serverLevel.getBlockState(areaFillerBlockPosition);

		if (blockEntity instanceof AreaFillerBlockEntity areaFillerBlockEntity) {
			areaFillerBlockEntity.setShowArea(showArea);
			areaFillerBlockEntity.setAreaDimensions(areaDimensions);
			areaFillerBlockEntity.setAreaPositionOffset(areaPositionOffset);
			if (!areaFillerBlockEntity.setBlockIdentifierString(blockIdentifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("area_filler_block.blockIdentifier.invalid"), false);
				updateSuccessful = false;
			}

			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			areaFillerBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(areaFillerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
