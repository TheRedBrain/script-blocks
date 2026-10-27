package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateTriggeredEntityRemoverBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredEntityRemoverBlockPacket> {

	@Override
	public void receive(UpdateTriggeredEntityRemoverBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredEntityRemoverBlockPosition = payload.triggeredEntityRemoverBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(triggeredEntityRemoverBlockPosition);
		BlockState blockState = world.getBlockState(triggeredEntityRemoverBlockPosition);

		if (blockEntity instanceof TriggeredEntityRemoverBlockEntity triggeredEntityRemoverBlockEntity) {
			triggeredEntityRemoverBlockEntity.setShowArea(showArea);
			triggeredEntityRemoverBlockEntity.setAreaDimensions(areaDimensions);
			triggeredEntityRemoverBlockEntity.setAreaPositionOffset(areaPositionOffset);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			triggeredEntityRemoverBlockEntity.setChanged();
			world.sendBlockUpdated(triggeredEntityRemoverBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}

	}
}
