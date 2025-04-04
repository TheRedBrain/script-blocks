package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTriggeredVillagerSpawnerBlockPacket(
		BlockPos triggeredSpawnerBlockPosition,
		BlockPos entitySpawnPositionOffset,
		double entitySpawnOrientationPitch,
		double entitySpawnOrientationYaw,
		String spawningMode,
		String entityTypeId,
		VillagerData villagerData,
		List<MutablePair<Identifier, EntityAttributeModifier>> entityAttributeModifiersList,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos useRelayBlockPositionOffset
) implements CustomPayload {
	public static final Id<UpdateTriggeredVillagerSpawnerBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggered_villager_spawner_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredVillagerSpawnerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredVillagerSpawnerBlockPacket::write, UpdateTriggeredVillagerSpawnerBlockPacket::new);

	public UpdateTriggeredVillagerSpawnerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.decodeAsJson(VillagerData.CODEC),
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

		registryByteBuf.encodeAsJson(VillagerData.CODEC, this.villagerData);

		registryByteBuf.writeCollection(this.entityAttributeModifiersList, CustomPacketCodecs.MUTABLE_PAIR_IDENTIFIER_ENTITY_ATTRIBUTE_MODIFIER);

		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);

		registryByteBuf.writeBlockPos(this.useRelayBlockPositionOffset);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
