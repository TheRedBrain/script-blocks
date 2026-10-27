package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Deprecated
public record UpdateAreaBlockPacket(
		BlockPos areaBlockPosition,
		boolean showArea,
		Vec3i applicationAreaDimensions,
		BlockPos applicationAreaPositionOffset,
		String appliedStatusEffectIdentifier,
		int appliedStatusEffectAmplifier,
		boolean appliedStatusEffectAmbient,
		boolean appliedStatusEffectShowParticles,
		boolean appliedStatusEffectShowIcon,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		boolean wasTriggered,
		String joinMessage,
		String leaveMessage,
		String triggeredMessage,
		String messageMode,
		String triggerMode,
		String triggeredMode,
		int timer
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateAreaBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_area_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateAreaBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateAreaBlockPacket::write, UpdateAreaBlockPacket::new);

	public UpdateAreaBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
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
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.areaBlockPosition);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.applicationAreaDimensions.getX());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getY());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.applicationAreaPositionOffset);
		registryByteBuf.writeUtf(this.appliedStatusEffectIdentifier);
		registryByteBuf.writeInt(this.appliedStatusEffectAmplifier);
		registryByteBuf.writeBoolean(this.appliedStatusEffectAmbient);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowParticles);
		registryByteBuf.writeBoolean(this.appliedStatusEffectShowIcon);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBoolean(this.wasTriggered);
		registryByteBuf.writeUtf(this.joinMessage);
		registryByteBuf.writeUtf(this.leaveMessage);
		registryByteBuf.writeUtf(this.triggeredMessage);
		registryByteBuf.writeUtf(this.messageMode);
		registryByteBuf.writeUtf(this.triggerMode);
		registryByteBuf.writeUtf(this.triggeredMode);
		registryByteBuf.writeInt(this.timer);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
