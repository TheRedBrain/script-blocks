package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;

public class UpdateBossControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPacketHandler<UpdateBossControllerBlockPacket> {

    @Override
    public void receive(UpdateBossControllerBlockPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        if (!player.isCreativeLevelTwoOp()) {
            return;
        }

        BlockPos bossControllerBlockPosition = packet.bossControllerBlockPosition;

        boolean showArea = packet.showArea;
        Vec3i areaDimensions = packet.applicationAreaDimensions;
        BlockPos areaPositionOffset = packet.applicationAreaPositionOffset;

        String bossIdentifier = packet.bossIdentifier;
        BlockPos entitySpawnPositionOffset = packet.entitySpawnPositionOffset;
        double entitySpawnOrientationPitch = packet.entitySpawnOrientationPitch;
        double entitySpawnOrientationYaw = packet.entitySpawnOrientationYaw;

        List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList = packet.bossTriggeredBlocksList;
        HashMap<String, MutablePair<BlockPos, Boolean>> bossTriggeredBlocksMap = new HashMap<>();
        for (MutablePair<String, MutablePair<BlockPos, Boolean>> triggeredBlock : bossTriggeredBlocksList) {
            bossTriggeredBlocksMap.put(triggeredBlock.getLeft(), triggeredBlock.getRight());
        }

        World world = player.getWorld();

        boolean updateSuccessful = true;

        BlockEntity blockEntity = world.getBlockEntity(bossControllerBlockPosition);
        BlockState blockState = world.getBlockState(bossControllerBlockPosition);

        if (blockEntity instanceof BossControllerBlockEntity bossControllerBlockEntity) {
            bossControllerBlockEntity.reset();
            bossControllerBlockEntity.setShowArea(showArea);
            if (!bossControllerBlockEntity.setAreaDimensions(areaDimensions)) {
                player.sendMessage(Text.translatable("area_block.areaDimensions.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setAreaPositionOffset(areaPositionOffset)) {
                player.sendMessage(Text.translatable("area_block.areaPositionOffset.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setBossIdentifier(bossIdentifier)) {
                player.sendMessage(Text.translatable("shop_block.bossIdentifier.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setEntitySpawnPositionOffset(entitySpawnPositionOffset)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnPositionOffset.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setEntitySpawnPositionPitch(entitySpawnOrientationPitch)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationPitch.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setEntitySpawnPositionYaw(entitySpawnOrientationYaw)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationYaw.invalid"), false);
                updateSuccessful = false;
            }
            if (!bossControllerBlockEntity.setBossTriggeredBlocks(bossTriggeredBlocksMap)) {
                player.sendMessage(Text.translatable("dialogue_block.bossTriggeredBlocksList.invalid"), false);
                updateSuccessful = false;
            }
            if (updateSuccessful) {
                player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
            }
            bossControllerBlockEntity.markDirty();
            world.updateListeners(bossControllerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
        }

    }
}
