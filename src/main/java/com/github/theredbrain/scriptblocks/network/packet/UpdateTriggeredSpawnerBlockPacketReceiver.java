package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateTriggeredSpawnerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTriggeredSpawnerBlockPacket> {
	@Override
	public void receive(UpdateTriggeredSpawnerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos triggeredSpawnerBlockPosition = payload.triggeredSpawnerBlockPosition();

		LevelData.RespawnData spawnPoint = payload.spawnPoint();

		TriggeredSpawnerBlockEntity.SpawningMode spawningMode = TriggeredSpawnerBlockEntity.SpawningMode.byName(payload.spawningMode()).orElse(TriggeredSpawnerBlockEntity.SpawningMode.BOUND);

		boolean isPersistent = payload.isPersistent();
		boolean enableSpawnEffects = payload.enableSpawnEffects();

		String entityTypeId = payload.entityTypeId();

		List<MutablePair<Identifier, AttributeModifier>> entityAttributeModifiersList = payload.entityAttributeModifiersList();

		Multimap<Holder<Attribute>, AttributeModifier> entityAttributeModifiers = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);
		for (MutablePair<Identifier, AttributeModifier> entityAttributeModifiersListEntry : entityAttributeModifiersList) {
			Optional<Holder.Reference<Attribute>> optional = BuiltInRegistries.ATTRIBUTE.get(entityAttributeModifiersListEntry.getLeft());
			optional.ifPresent(entityAttributeReference -> entityAttributeModifiers.put(entityAttributeReference, entityAttributeModifiersListEntry.getRight()));
		}

		BlockPos useRelayBlockPositionOffset = payload.useRelayBlockPositionOffset();

		BlockPos triggeredBlockPositionOffset = payload.triggeredBlockPositionOffset();
		boolean triggeredBlockResets = payload.triggeredBlockResets();

		ServerLevel serverLevel = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = serverLevel.getBlockEntity(triggeredSpawnerBlockPosition);
		BlockState blockState = serverLevel.getBlockState(triggeredSpawnerBlockPosition);

		if (blockEntity instanceof TriggeredSpawnerBlockEntity triggeredSpawnerBlockEntity) {
			triggeredSpawnerBlockEntity.reset();
			triggeredSpawnerBlockEntity.setSpawnPoint(spawnPoint);
			triggeredSpawnerBlockEntity.setSpawningMode(spawningMode);
			triggeredSpawnerBlockEntity.setIsPersistent(isPersistent);
			triggeredSpawnerBlockEntity.setEnableSpawnEffects(enableSpawnEffects);
			if (!triggeredSpawnerBlockEntity.setEntityType(entityTypeId)) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("triggered_spawner_block.entity_type_id.invalid"), false);
				updateSuccessful = false;
			}
			triggeredSpawnerBlockEntity.setEntityAttributeModifiers(entityAttributeModifiers);
			triggeredSpawnerBlockEntity.setTriggeredBlock(new MutablePair<>(triggeredBlockPositionOffset, triggeredBlockResets));
			triggeredSpawnerBlockEntity.setUseRelayBlockPositionOffset(useRelayBlockPositionOffset);
			if (updateSuccessful) {
				serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			triggeredSpawnerBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(triggeredSpawnerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
