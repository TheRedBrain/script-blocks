package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateMimicBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateMimicBlockPacket> {
	@Override
	public void receive(UpdateMimicBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos mimicBlockPosition = payload.mimicBlockPosition();

		BlockPos activeMimicBlockPositionOffset = payload.activeMimicBlockPositionOffset();
		BlockPos inactiveMimicBlockPositionOffset = payload.inactiveMimicBlockPositionOffset();

		Level world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(mimicBlockPosition);
		BlockState blockState = world.getBlockState(mimicBlockPosition);

		if (blockEntity instanceof MimicBlockEntity mimicBlockEntity) {
			mimicBlockEntity.reset();
			if (!mimicBlockEntity.setActiveMimicBlockPositionOffset(activeMimicBlockPositionOffset)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("mimic_block.activeMimicBlockPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!mimicBlockEntity.setInactiveMimicBlockPositionOffset(inactiveMimicBlockPositionOffset)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("mimic_block.inactiveMimicBlockPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			mimicBlockEntity.setChanged();
			world.sendBlockUpdated(mimicBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
