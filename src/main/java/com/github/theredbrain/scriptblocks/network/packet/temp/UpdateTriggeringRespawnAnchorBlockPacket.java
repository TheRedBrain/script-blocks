package com.github.theredbrain.scriptblocks.network.packet.temp;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateTriggeringRespawnAnchorBlockPacket(
		BlockPos triggeringRespawnAnchorBlockPosition,
		BlockPos spawnPointPositionOffset,
		double spawnPointYaw,
		double spawnPointPitch,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets
) implements CustomPayload {
	public static final Id<UpdateTriggeringRespawnAnchorBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggering_respawn_anchor_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeringRespawnAnchorBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeringRespawnAnchorBlockPacket::write, UpdateTriggeringRespawnAnchorBlockPacket::new);

	public UpdateTriggeringRespawnAnchorBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeringRespawnAnchorBlockPosition);
		registryByteBuf.writeBlockPos(this.spawnPointPositionOffset);
		registryByteBuf.writeDouble(this.spawnPointYaw);
		registryByteBuf.writeDouble(this.spawnPointPitch);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
