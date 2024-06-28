package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.registry.StatusEffectsRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class SetHousingBlockOwnerPacketReceiver implements ServerPlayNetworking.PlayPacketHandler<SetHousingBlockOwnerPacket> {
	@Override
	public void receive(SetHousingBlockOwnerPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

		BlockPos housingBlockPosition = packet.housingBlockPosition;

		String owner = packet.owner;

		World world = player.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
		BlockState blockState = world.getBlockState(housingBlockPosition);

		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

			if (!housingBlockEntity.setOwnerUuid(owner)) {
				player.sendMessage(Text.translatable("housing_block.owner.invalid"), false);
				updateSuccessful = false;
			}
			if (updateSuccessful) {
				if (Objects.equals(owner, "")) {
					housingBlockEntity.setIsOwnerSet(false);
					player.sendMessage(Text.translatable("housing_block.unclaimed_successful"), true);
					player.removeStatusEffect(StatusEffectsRegistry.HOUSING_OWNER_EFFECT);
					player.removeStatusEffect(StatusEffectsRegistry.BUILDING_MODE);
				} else {
					housingBlockEntity.setIsOwnerSet(true);
					player.sendMessage(Text.translatable("housing_block.claimed_successful"), true);
				}
			}
			housingBlockEntity.markDirty();
			world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
