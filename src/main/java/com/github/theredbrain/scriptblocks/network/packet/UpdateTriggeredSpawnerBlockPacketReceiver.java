package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateTriggeredSpawnerBlockPacketReceiver implements ServerPlayNetworking.PlayPacketHandler<UpdateTriggeredSpawnerBlockPacket> {
    @Override
    public void receive(UpdateTriggeredSpawnerBlockPacket packet, ServerPlayerEntity player, PacketSender responseSender) {

        if (!player.isCreativeLevelTwoOp()) {
            return;
        }

        BlockPos triggeredSpawnerBlockPosition = packet.triggeredSpawnerBlockPosition;

        BlockPos entitySpawnPositionOffset = packet.entitySpawnPositionOffset;
        double entitySpawnOrientationPitch = packet.entitySpawnOrientationPitch;
        double entitySpawnOrientationYaw = packet.entitySpawnOrientationYaw;

        TriggeredSpawnerBlockEntity.SpawningMode spawningMode = packet.spawningMode;

        String entityTypeId = packet.entityTypeId;

        List<MutablePair<String, EntityAttributeModifier>> entityAttributeModifiersList = packet.entityAttributeModifiersList;
        Multimap<EntityAttribute, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
        for (MutablePair<String, EntityAttributeModifier> entityAttributeModifiersListEntry : entityAttributeModifiersList) {
            Optional<EntityAttribute> optional = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(entityAttributeModifiersListEntry.getLeft()));
            optional.ifPresent(entityAttribute -> entityAttributeModifiers.put(entityAttribute, entityAttributeModifiersListEntry.getRight()));
        }

        BlockPos useRelayBlockPositionOffset = packet.useRelayBlockPositionOffset;

        BlockPos triggeredBlockPositionOffset = packet.triggeredBlockPositionOffset;
        boolean triggeredBlockResets = packet.triggeredBlockResets;

        World world = player.getWorld();

        boolean updateSuccessful = true;

        BlockEntity blockEntity = world.getBlockEntity(triggeredSpawnerBlockPosition);
        BlockState blockState = world.getBlockState(triggeredSpawnerBlockPosition);

        if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
            triggeredSpawnerBlockEntity.reset();
            if (!triggeredSpawnerBlockEntity.setEntitySpawnPositionOffset(entitySpawnPositionOffset)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnPositionOffset.invalid"), false);
                updateSuccessful = false;
            }
            if (!triggeredSpawnerBlockEntity.setEntitySpawnPositionPitch(entitySpawnOrientationPitch)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationPitch.invalid"), false);
                updateSuccessful = false;
            }
            if (!triggeredSpawnerBlockEntity.setEntitySpawnPositionYaw(entitySpawnOrientationYaw)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationYaw.invalid"), false);
                updateSuccessful = false;
            }
            if (!triggeredSpawnerBlockEntity.setSpawningMode(spawningMode)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.spawningMode.invalid"), false);
                updateSuccessful = false;
            }
            if (!triggeredSpawnerBlockEntity.setEntityType(entityTypeId)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entityTypeId.invalid"), false);
                updateSuccessful = false;
            }
            if (!triggeredSpawnerBlockEntity.setEntityAttributeModifiers(entityAttributeModifiers)) {
                player.sendMessage(Text.translatable("triggered_spawner_block.entityAttributeModifiers.invalid"), false);
                updateSuccessful = false;
            }
            triggeredSpawnerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
            triggeredSpawnerBlockEntity.setUseRelayBlockPositionOffset(useRelayBlockPositionOffset);
            if (updateSuccessful) {
                player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
            }
            triggeredSpawnerBlockEntity.markDirty();
            world.updateListeners(triggeredSpawnerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
        }
    }
}
