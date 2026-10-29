package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UpdateShopBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateShopBlockPacket> {

	@Override
	public void receive(UpdateShopBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos shopBlockPosition = payload.shopBlockPosition();
		String shopIdentifier = payload.shopIdentifier();

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(shopBlockPosition);
		BlockState blockState = serverLevel.getBlockState(shopBlockPosition);

		if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {

			if (!shopBlockEntity.setShopIdentifier(shopIdentifier)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("shop_block.shopIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			shopBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(shopBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}

	}
}
