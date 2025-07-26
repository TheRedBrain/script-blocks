package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class UpdateAreaFillerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateAreaFillerBlockPacket> {
	@Override
	public void receive(UpdateAreaFillerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos areaFillerBlockPosition = payload.areaFillerBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		String blockIdentifier = payload.blockIdentifier();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(areaFillerBlockPosition);
		BlockState blockState = world.getBlockState(areaFillerBlockPosition);

		if (blockEntity instanceof AreaFillerBlockEntity areaFillerBlockEntity) {
			areaFillerBlockEntity.setShowArea(showArea);
			if (!areaFillerBlockEntity.setAreaDimensions(areaDimensions)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_filler_block.areaDimensions.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaFillerBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_filler_block.areaPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!areaFillerBlockEntity.setBlockIdentifierString(blockIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("area_filler_block.blockIdentifier.invalid"), false);
				updateSuccessful = false;
			}

			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			areaFillerBlockEntity.markDirty();
			world.updateListeners(areaFillerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
