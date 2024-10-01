package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateAreaBlockPacket(BlockPos areaBlockPosition, boolean showArea, Vec3i applicationAreaDimensions,
									BlockPos applicationAreaPositionOffset, String appliedStatusEffectIdentifier,
									int appliedStatusEffectAmplifier, boolean appliedStatusEffectAmbient,
									boolean appliedStatusEffectShowParticles, boolean appliedStatusEffectShowIcon,
									BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets,
									boolean wasTriggered, String joinMessage, String leaveMessage,
									String triggeredMessage, String messageMode, String triggerMode,
									String triggeredMode, int timer) implements CustomPayload {
	public static final CustomPayload.Id<UpdateAreaBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_area_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateAreaBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateAreaBlockPacket::write, UpdateAreaBlockPacket::new);

	public UpdateAreaBlockPacket(RegistryByteBuf registryByteBuf) {
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
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.areaBlockPosition);
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
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBoolean(this.wasTriggered);
		registryByteBuf.writeString(this.joinMessage);
		registryByteBuf.writeString(this.leaveMessage);
		registryByteBuf.writeString(this.triggeredMessage);
		registryByteBuf.writeString(this.messageMode);
		registryByteBuf.writeString(this.triggerMode);
		registryByteBuf.writeString(this.triggeredMode);
		registryByteBuf.writeInt(this.timer);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
