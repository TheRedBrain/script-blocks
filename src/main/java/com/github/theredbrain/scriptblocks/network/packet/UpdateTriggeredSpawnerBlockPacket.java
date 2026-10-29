package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record UpdateTriggeredSpawnerBlockPacket(
		BlockPos triggeredSpawnerBlockPosition,
		LevelData.RespawnData spawnPoint,
		String spawningMode,
		boolean isPersistent,
		boolean enableSpawnEffects,
		String entityTypeId,
		List<MutablePair<Identifier, AttributeModifier>> entityAttributeModifiersList,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos useRelayBlockPositionOffset
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateTriggeredSpawnerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_triggered_spawner_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredSpawnerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredSpawnerBlockPacket::write, UpdateTriggeredSpawnerBlockPacket::new);

	public UpdateTriggeredSpawnerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredSpawnerBlockPosition);

		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.spawnPoint);

		registryByteBuf.writeUtf(this.spawningMode);

		registryByteBuf.writeBoolean(this.isPersistent);
		registryByteBuf.writeBoolean(this.enableSpawnEffects);

		registryByteBuf.writeUtf(this.entityTypeId);

		registryByteBuf.writeCollection(this.entityAttributeModifiersList, CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER);

		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);

		registryByteBuf.writeBlockPos(this.useRelayBlockPositionOffset);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
