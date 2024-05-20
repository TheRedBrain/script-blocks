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

public class UpdateHousingBlockAdventurePacketReceiver implements ServerPlayNetworking.PlayPacketHandler<UpdateHousingBlockAdventurePacket> {
    @Override
    public void receive(UpdateHousingBlockAdventurePacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        if (!player.isCreativeLevelTwoOp()) {
            return;
        }

        BlockPos housingBlockPosition = packet.housingBlockPosition;

        List<String> coOwnerList = packet.coOwnerList;
        List<String> trustedList = packet.trustedList;
        List<String> guestList = packet.guestList;

        World world = player.getWorld();

        boolean updateSuccessful = true;

        BlockEntity blockEntity = world.getBlockEntity(housingBlockPosition);
        BlockState blockState = world.getBlockState(housingBlockPosition);

        if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {

            if (!housingBlockEntity.setCoOwnerList(coOwnerList)) {
                player.sendMessage(Text.translatable("housing_block.coOwnerList.invalid"), false);
                updateSuccessful = false;
            }
            if (!housingBlockEntity.setTrustedList(trustedList)) {
                player.sendMessage(Text.translatable("housing_block.trustedList.invalid"), false);
                updateSuccessful = false;
            }
            if (!housingBlockEntity.setGuestList(guestList)) {
                player.sendMessage(Text.translatable("housing_block.guestList.invalid"), false);
                updateSuccessful = false;
            }
            if (updateSuccessful) {
                player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
            }
            housingBlockEntity.markDirty();
            world.updateListeners(housingBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
        }
    }
}
