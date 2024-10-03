package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTriggeredSpawnerBlockPacket(BlockPos triggeredSpawnerBlockPosition,
												BlockPos entitySpawnPositionOffset, double entitySpawnOrientationPitch,
												double entitySpawnOrientationYaw, String spawningMode,
												String entityTypeId,
												List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList,
												BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets,
												BlockPos useRelayBlockPositionOffset) implements CustomPayload {
	public static final CustomPayload.Id<UpdateTriggeredSpawnerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_triggered_spawner_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredSpawnerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredSpawnerBlockPacket::write, UpdateTriggeredSpawnerBlockPacket::new);

	public UpdateTriggeredSpawnerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredSpawnerBlockPosition);

		registryByteBuf.writeBlockPos(this.entitySpawnPositionOffset);
		registryByteBuf.writeDouble(this.entitySpawnOrientationPitch);
		registryByteBuf.writeDouble(this.entitySpawnOrientationYaw);

		registryByteBuf.writeString(this.spawningMode);

		registryByteBuf.writeString(this.entityTypeId);

		registryByteBuf.writeCollection(this.entityAttributeModifiersList, CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER);

		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);

		registryByteBuf.writeBlockPos(this.useRelayBlockPositionOffset);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
