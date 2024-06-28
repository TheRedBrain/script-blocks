package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.util.PacketByteBufUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateTriggeredSpawnerBlockPacket implements FabricPacket {
	public static final PacketType<UpdateTriggeredSpawnerBlockPacket> TYPE = PacketType.create(
			ScriptBlocksMod.identifier("update_triggered_spawner_block"),
			UpdateTriggeredSpawnerBlockPacket::new
	);

	public final BlockPos triggeredSpawnerBlockPosition;
	public final BlockPos entitySpawnPositionOffset;
	public final double entitySpawnOrientationPitch;
	public final double entitySpawnOrientationYaw;
	public final TriggeredSpawnerBlockEntity.SpawningMode spawningMode;
	public final String entityTypeId;
	public final List<MutablePair<String, EntityAttributeModifier>> entityAttributeModifiersList;
	public final BlockPos triggeredBlockPositionOffset;
	public final boolean triggeredBlockResets;
	public final BlockPos useRelayBlockPositionOffset;


	public UpdateTriggeredSpawnerBlockPacket(BlockPos triggeredSpawnerBlockPosition, BlockPos entitySpawnPositionOffset, double entitySpawnOrientationPitch, double entitySpawnOrientationYaw, String spawningMode, String entityTypeId, List<MutablePair<String, EntityAttributeModifier>> entityAttributeModifiersList, BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets, BlockPos useRelayBlockPositionOffset) {
		this.triggeredSpawnerBlockPosition = triggeredSpawnerBlockPosition;

		this.entitySpawnPositionOffset = entitySpawnPositionOffset;
		this.entitySpawnOrientationPitch = entitySpawnOrientationPitch;
		this.entitySpawnOrientationYaw = entitySpawnOrientationYaw;

		this.spawningMode = TriggeredSpawnerBlockEntity.SpawningMode.byName(spawningMode).orElseGet(() -> TriggeredSpawnerBlockEntity.SpawningMode.ONCE);

		this.entityTypeId = entityTypeId;

		this.entityAttributeModifiersList = entityAttributeModifiersList;

		this.triggeredBlockPositionOffset = triggeredBlockPositionOffset;
		this.triggeredBlockResets = triggeredBlockResets;

		this.useRelayBlockPositionOffset = useRelayBlockPositionOffset;
	}

	public UpdateTriggeredSpawnerBlockPacket(PacketByteBuf buf) {
		this(
				buf.readBlockPos(),
				buf.readBlockPos(),
				buf.readDouble(),
				buf.readDouble(),
				buf.readString(),
				buf.readString(),
				buf.readList(new PacketByteBufUtils.MutablePairStringEntityAttributeModifierReader()),
				buf.readBlockPos(),
				buf.readBoolean(),
				buf.readBlockPos()
		);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.triggeredSpawnerBlockPosition);

		buf.writeBlockPos(this.entitySpawnPositionOffset);
		buf.writeDouble(this.entitySpawnOrientationPitch);
		buf.writeDouble(this.entitySpawnOrientationYaw);

		buf.writeString(this.spawningMode.asString());

		buf.writeString(this.entityTypeId);

		buf.writeCollection(this.entityAttributeModifiersList, new PacketByteBufUtils.MutablePairStringEntityAttributeModifierWriter());

		buf.writeBlockPos(this.triggeredBlockPositionOffset);
		buf.writeBoolean(this.triggeredBlockResets);

		buf.writeBlockPos(this.useRelayBlockPositionOffset);
	}
}
