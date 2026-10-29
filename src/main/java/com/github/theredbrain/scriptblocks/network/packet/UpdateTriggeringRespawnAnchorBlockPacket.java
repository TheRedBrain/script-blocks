package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;

public record UpdateTriggeringRespawnAnchorBlockPacket(
		BlockPos triggeringRespawnAnchorBlockPosition,
		LevelData.RespawnData spawnPoint,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		boolean sendMessage
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeringRespawnAnchorBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggering_respawn_anchor_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeringRespawnAnchorBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeringRespawnAnchorBlockPacket::write, UpdateTriggeringRespawnAnchorBlockPacket::new);

	public UpdateTriggeringRespawnAnchorBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeringRespawnAnchorBlockPosition);
		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.spawnPoint);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBoolean(this.sendMessage);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
