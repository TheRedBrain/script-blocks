package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
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

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(areaFillerBlockPosition);
		BlockState blockState = world.getBlockState(areaFillerBlockPosition);

		if (blockEntity instanceof AreaFillerBlockEntity areaFillerBlockEntity) {
			areaFillerBlockEntity.setShowArea(showArea);
			areaFillerBlockEntity.setAreaDimensions(areaDimensions);
			areaFillerBlockEntity.setAreaPositionOffset(areaPositionOffset);
			if (!areaFillerBlockEntity.setBlockIdentifierString(blockIdentifier)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("area_filler_block.blockIdentifier.invalid"), false);
				updateSuccessful = false;
			}

			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			areaFillerBlockEntity.setChanged();
			world.sendBlockUpdated(areaFillerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
