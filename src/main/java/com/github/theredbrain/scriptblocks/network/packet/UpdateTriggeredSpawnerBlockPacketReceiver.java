package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
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
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateTriggeredSpawnerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredSpawnerBlockPacket> {
	@Override
	public void receive(UpdateTriggeredSpawnerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredSpawnerBlockPosition = payload.triggeredSpawnerBlockPosition();

		BlockPos entitySpawnPositionOffset = payload.entitySpawnPositionOffset();
		double entitySpawnOrientationPitch = payload.entitySpawnOrientationPitch();
		double entitySpawnOrientationYaw = payload.entitySpawnOrientationYaw();

		TriggeredSpawnerBlockEntity.SpawningMode spawningMode = TriggeredSpawnerBlockEntity.SpawningMode.byName(payload.spawningMode()).orElse(TriggeredSpawnerBlockEntity.SpawningMode.BOUND);

		boolean isPersistent = payload.isPersistent();
		boolean enableSpawnEffects = payload.enableSpawnEffects();

		String entityTypeId = payload.entityTypeId();

		List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList = payload.entityAttributeModifiersList();

		Multimap<RegistryEntry<EntityAttribute>, EntityAttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		for (MutablePair<Identifier, EntityAttributeModifier> entityAttributeModifiersListEntry : entityAttributeModifiersList) {
			Optional<RegistryEntry.Reference<EntityAttribute>> optional = Registries.ATTRIBUTE.getEntry(entityAttributeModifiersListEntry.getLeft());
			optional.ifPresent(entityAttributeReference -> entityAttributeModifiers.put(entityAttributeReference, entityAttributeModifiersListEntry.getRight()));
		}

		BlockPos useRelayBlockPositionOffset = payload.useRelayBlockPositionOffset();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		World world = serverPlayerEntity.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(triggeredSpawnerBlockPosition);
		BlockState blockState = world.getBlockState(triggeredSpawnerBlockPosition);

		if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
			triggeredSpawnerBlockEntity.reset();
			triggeredSpawnerBlockEntity.setEntitySpawnPositionOffset(entitySpawnPositionOffset);
			if (!triggeredSpawnerBlockEntity.setEntitySpawnPositionPitch(entitySpawnOrientationPitch)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entity_spawn_orientation_pitch.invalid"), false);
				updateSuccessful = false;
			}
			if (!triggeredSpawnerBlockEntity.setEntitySpawnPositionYaw(entitySpawnOrientationYaw)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entity_spawn_orientation_yaw.invalid"), false);
				updateSuccessful = false;
			}
			triggeredSpawnerBlockEntity.setSpawningMode(spawningMode);
			triggeredSpawnerBlockEntity.setIsPersistent(isPersistent);
			triggeredSpawnerBlockEntity.setEnableSpawnEffects(enableSpawnEffects);
			if (!triggeredSpawnerBlockEntity.setEntityType(entityTypeId)) {
				serverPlayerEntity.sendMessage(Text.translatable("triggered_spawner_block.entity_type_id.invalid"), false);
				updateSuccessful = false;
			}
			triggeredSpawnerBlockEntity.setEntityAttributeModifiers(entityAttributeModifiers);
			triggeredSpawnerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			triggeredSpawnerBlockEntity.setUseRelayBlockPositionOffset(useRelayBlockPositionOffset);
			if (updateSuccessful) {
				serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredSpawnerBlockEntity.markDirty();
			world.updateListeners(triggeredSpawnerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
