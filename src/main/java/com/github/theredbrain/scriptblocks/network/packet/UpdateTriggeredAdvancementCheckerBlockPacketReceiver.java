package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class UpdateTriggeredAdvancementCheckerBlockPacketReceiver implements ServerPlayNetworking.PlayPacketHandler<UpdateTriggeredAdvancementCheckerBlockPacket> {
    @Override
    public void receive(UpdateTriggeredAdvancementCheckerBlockPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        if (!player.isCreativeLevelTwoOp()) {
            return;
        }

        BlockPos triggeredAdvancementCheckerBlockPosition = packet.triggeredAdvancementCheckerBlockPosition;

        BlockPos firstTriggeredBlockPositionOffset = packet.firstTriggeredBlockPositionOffset;

        boolean firstTriggeredBlockResets = packet.firstTriggeredBlockResets;

        BlockPos secondTriggeredBlockPositionOffset = packet.secondTriggeredBlockPositionOffset;

        boolean secondTriggeredBlockResets = packet.secondTriggeredBlockResets;

        String checkedAdvancementIdentifier = packet.checkedAdvancementIdentifier;

        World world = player.getWorld();

        boolean updateSuccessful = true;

        BlockEntity blockEntity = world.getBlockEntity(triggeredAdvancementCheckerBlockPosition);
        BlockState blockState = world.getBlockState(triggeredAdvancementCheckerBlockPosition);

        if (blockEntity instanceof TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlockEntity) {
            triggeredAdvancementCheckerBlockEntity.setFirstTriggeredBlock(new MutablePair<>(firstTriggeredBlockPositionOffset, firstTriggeredBlockResets));
            triggeredAdvancementCheckerBlockEntity.setSecondTriggeredBlock(new MutablePair<>(secondTriggeredBlockPositionOffset, secondTriggeredBlockResets));
            if (!triggeredAdvancementCheckerBlockEntity.setCheckedAdvancementIdentifier(checkedAdvancementIdentifier)) {
                player.sendMessage(Text.translatable("triggered_advancement_checker_block.checkedAdvancementIdentifier.invalid"), false);
                updateSuccessful = false;
            }
            if (updateSuccessful) {
                player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
            }
            triggeredAdvancementCheckerBlockEntity.markDirty();
            world.updateListeners(triggeredAdvancementCheckerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
        }
    }
}
