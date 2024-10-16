package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class UpdateHousingBlockAdventurePacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateHousingBlockAdventurePacket> {
	@Override
	public void receive(UpdateHousingBlockAdventurePacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		BlockPos housingBlockPosition = payload.housingBlockPosition();

		List<String> coOwnerList = new ArrayList<>(payload.coOwnerList());
		List<String> trustedList = new ArrayList<>(payload.trustedList());
		List<String> guestList = new ArrayList<>(payload.guestList());

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {
			housingBlockEntity.setCoOwnerList(coOwnerList);
			housingBlockEntity.setTrustedList(trustedList);
			housingBlockEntity.setGuestList(guestList);
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			housingBlockEntity.markDirty();
			world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
