package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTriggeredVillagerSpawnerBlockPacket(
		BlockPos triggeredSpawnerBlockPosition,
		LevelData.RespawnData spawnPoint,
		TriggeredSpawnerBlockEntity.SpawningMode spawningMode,
		boolean isPersistent,
		boolean enableSpawnEffects,
		String entityTypeId,
		VillagerData villagerData,
		List<MutablePair<Identifier, AttributeModifier>> entityAttributeModifiersList,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos useRelayBlockPositionOffset
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredVillagerSpawnerBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_villager_spawner_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredVillagerSpawnerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredVillagerSpawnerBlockPacket::write, UpdateTriggeredVillagerSpawnerBlockPacket::new);

	public UpdateTriggeredVillagerSpawnerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readEnum(TriggeredSpawnerBlockEntity.SpawningMode.class),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				VillagerData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredSpawnerBlockPosition);

		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.spawnPoint);

		registryByteBuf.writeEnum(this.spawningMode);

		registryByteBuf.writeBoolean(this.isPersistent);
		registryByteBuf.writeBoolean(this.enableSpawnEffects);

		registryByteBuf.writeUtf(this.entityTypeId);

		VillagerData.STREAM_CODEC.encode(registryByteBuf, this.villagerData);

		registryByteBuf.writeCollection(this.entityAttributeModifiersList, CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER);

		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);

		registryByteBuf.writeBlockPos(this.useRelayBlockPositionOffset);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
