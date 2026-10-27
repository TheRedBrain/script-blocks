package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

public class UpdateHousingBlockAdventurePacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateHousingBlockAdventurePacket> {
	@Override
	public void receive(UpdateHousingBlockAdventurePacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		List<String> coOwnerList = new ArrayList<>(payload.coOwnerList());
		List<String> trustedList = new ArrayList<>(payload.trustedList());
		List<String> guestList = new ArrayList<>(payload.guestList());

		Level world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {
			housingBlockEntity.setCoOwnerList(coOwnerList);
			housingBlockEntity.setTrustedList(trustedList);
			housingBlockEntity.setGuestList(guestList);
			serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			housingBlockEntity.setChanged();
			world.sendBlockUpdated(housingBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
