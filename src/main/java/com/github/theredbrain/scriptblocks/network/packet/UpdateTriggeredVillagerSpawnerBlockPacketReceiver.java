package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateTriggeredVillagerSpawnerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredVillagerSpawnerBlockPacket> {
	@Override
	public void receive(UpdateTriggeredVillagerSpawnerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos triggeredSpawnerBlockPosition = payload.triggeredSpawnerBlockPosition();

		BlockPos entitySpawnPositionOffset = payload.entitySpawnPositionOffset();
		double entitySpawnOrientationPitch = payload.entitySpawnOrientationPitch();
		double entitySpawnOrientationYaw = payload.entitySpawnOrientationYaw();

		TriggeredSpawnerBlockEntity.SpawningMode spawningMode = TriggeredSpawnerBlockEntity.SpawningMode.byName(payload.spawningMode()).orElse(TriggeredSpawnerBlockEntity.SpawningMode.BOUND);

		String entityTypeId = payload.entityTypeId();

		VillagerData villagerData = payload.villagerData();

		List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList = payload.entityAttributeModifiersList();
		Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		for (MutablePair<Identifier, EntityAttributeModifier> entityAttributeModifiersListEntry : entityAttributeModifiersList) {
			Optional<RegistryEntry.Reference<EntityAttribute>> optional = Registries.ATTRIBUTE.getEntry(entityAttributeModifiersListEntry.getLeft());
			optional.ifPresent(entityAttribute -> entityAttributeModifiers.put(entityAttribute, entityAttributeModifiersListEntry.getRight()));
		}

		BlockPos useRelayBlockPositionOffset = payload.useRelayBlockPositionOffset();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredSpawnerBlockPosition);
		BlockState blockState = world.getBlockState(triggeredSpawnerBlockPosition);

		if (blockEntity instanceof TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlockEntity) {
			triggeredVillagerSpawnerBlockEntity.reset();
			if (!triggeredVillagerSpawnerBlockEntity.setEntitySpawnPositionOffset(entitySpawnPositionOffset)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnPositionOffset.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredVillagerSpawnerBlockEntity.setEntitySpawnPositionPitch(entitySpawnOrientationPitch)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationPitch.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredVillagerSpawnerBlockEntity.setEntitySpawnPositionYaw(entitySpawnOrientationYaw)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entitySpawnOrientationYaw.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredVillagerSpawnerBlockEntity.setSpawningMode(spawningMode)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.spawningMode.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredVillagerSpawnerBlockEntity.setEntityType(entityTypeId)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entityTypeId.invalid"), false);
				updateSuccessful = false;
			}
			triggeredVillagerSpawnerBlockEntity.setVillagerData(villagerData);
			if (!triggeredVillagerSpawnerBlockEntity.setEntityAttributeModifiers(entityAttributeModifiers)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entityAttributeModifiers.invalid"), false);
				updateSuccessful = false;
			}
			triggeredVillagerSpawnerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			triggeredVillagerSpawnerBlockEntity.setUseRelayBlockPositionOffset(useRelayBlockPositionOffset);
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredVillagerSpawnerBlockEntity.markDirty();
			world.updateListeners(triggeredSpawnerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
