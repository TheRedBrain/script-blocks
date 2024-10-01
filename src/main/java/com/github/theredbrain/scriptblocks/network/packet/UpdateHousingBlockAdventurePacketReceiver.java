package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class UpdateHousingBlockAdventurePacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateHousingBlockAdventurePacket> {
	@Override
	public void receive(UpdateHousingBlockAdventurePacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		List<String> coOwnerList = payload.coOwnerList();
		List<String> trustedList = payload.trustedList();
		List<String> guestList = payload.guestList();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			if (!housingBlockEntity.setCoOwnerList(coOwnerList)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.coOwnerList.invalid"), false);
				updateSuccessful = false;
			}
			if (!housingBlockEntity.setTrustedList(trustedList)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.trustedList.invalid"), false);
				updateSuccessful = false;
			}
			if (!housingBlockEntity.setGuestList(guestList)) {
				serverPlayerEntity.sendMessage(Text.translatable("housing_block.guestList.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			housingBlockEntity.markDirty();
			world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
