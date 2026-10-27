package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeringRespawnAnchorBlockPacket(
		BlockPos triggeringRespawnAnchorBlockPosition,
		BlockPos spawnPointPositionOffset,
		double spawnPointYaw,
		double spawnPointPitch,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		boolean sendMessage
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeringRespawnAnchorBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggering_respawn_anchor_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeringRespawnAnchorBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeringRespawnAnchorBlockPacket::write, UpdateTriggeringRespawnAnchorBlockPacket::new);

	public UpdateTriggeringRespawnAnchorBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeringRespawnAnchorBlockPosition);
		registryByteBuf.writeBlockPos(this.spawnPointPositionOffset);
		registryByteBuf.writeDouble(this.spawnPointYaw);
		registryByteBuf.writeDouble(this.spawnPointPitch);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBoolean(this.sendMessage);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
