package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UpdateShopBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateShopBlockPacket> {

	@Override
	public void receive(UpdateShopBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos shopBlockPosition = payload.shopBlockPosition();
		String shopIdentifier = payload.shopIdentifier();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(shopBlockPosition);
		BlockState blockState = world.getBlockState(shopBlockPosition);

		if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {

			if (!shopBlockEntity.setShopIdentifier(shopIdentifier)) {
				serverPlayerEntity.sendMessage(Text.translatable("shop_block.shopIdentifier.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			shopBlockEntity.markDirty();
			world.updateListeners(shopBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}

	}
}
