package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
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
		BlockPos entitySpawnPositionOffset,
		double entitySpawnOrientationPitch,
		double entitySpawnOrientationYaw,
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
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
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

		registryByteBuf.writeBlockPos(this.entitySpawnPositionOffset);
		registryByteBuf.writeDouble(this.entitySpawnOrientationPitch);
		registryByteBuf.writeDouble(this.entitySpawnOrientationYaw);

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
