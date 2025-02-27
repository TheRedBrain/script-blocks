package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateTriggeredBeaconBlockPacket(
		BlockPos triggeredBeaconBlockPosition,
		boolean showArea,
		Vec3i applicationAreaDimensions,
		BlockPos applicationAreaPositionOffset,
		String appliedStatusEffectIdentifier,
		int appliedStatusEffectAmplifier,
		boolean appliedStatusEffectAmbient,
		boolean appliedStatusEffectShowParticles,
		boolean appliedStatusEffectShowIcon,
		boolean triggered,
		String triggeredMode
) implements CustomPayload {
	public static final Id<UpdateTriggeredBeaconBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggered_beacon_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredBeaconBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredBeaconBlockPacket::write, UpdateTriggeredBeaconBlockPacket::new);

	public UpdateTriggeredBeaconBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredBeaconBlockPosition);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.applicationAreaDimensions.getX());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getY());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.applicationAreaPositionOffset);
		registryByteBuf.writeString(this.appliedStatusEffectIdentifier);
		registryByteBuf.writeInt(this.appliedStatusEffectAmplifier);
		registryByteBuf.writeBoolean(this.appliedStatusEffectAmbient);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowParticles);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowIcon);
		registryByteBuf.writeBoolean(this.triggered);
		registryByteBuf.writeString(this.triggeredMode);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
