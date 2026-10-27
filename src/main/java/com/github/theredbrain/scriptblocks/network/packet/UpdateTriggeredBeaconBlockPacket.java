package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeredBeaconBlockPacket(
		BlockPos triggeredBeaconBlockPosition,
		boolean showArea,
		Vec3i applicationAreaDimensions,
		BlockPos applicationAreaPositionOffset,
		String appliedStatusEffectIdentifier,
		int appliedStatusEffectAmplifier,
		int appliedStatusEffectDuration,
		boolean appliedStatusEffectAmbient,
		boolean appliedStatusEffectShowParticles,
		boolean appliedStatusEffectShowIcon,
		boolean triggered,
		String triggeredMode
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredBeaconBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_beacon_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredBeaconBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredBeaconBlockPacket::write, UpdateTriggeredBeaconBlockPacket::new);

	public UpdateTriggeredBeaconBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredBeaconBlockPosition);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.applicationAreaDimensions.getX());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getY());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.applicationAreaPositionOffset);
		registryByteBuf.writeUtf(this.appliedStatusEffectIdentifier);
		registryByteBuf.writeInt(this.appliedStatusEffectAmplifier);
		registryByteBuf.writeInt(this.appliedStatusEffectDuration);
		registryByteBuf.writeBoolean(this.appliedStatusEffectAmbient);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowParticles);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowIcon);
		registryByteBuf.writeBoolean(this.triggered);
		registryByteBuf.writeUtf(this.triggeredMode);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
