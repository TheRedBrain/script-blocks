package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateSpawnPointDelegationBlockPacket(
		BlockPos entranceDelegationBlockPosition,
		BlockPos delegatedEntrancePositionOffset,
		double delegatedEntranceYaw,
		double delegatedEntrancePitch
) implements CustomPayload {
	public static final Id<UpdateSpawnPointDelegationBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_spawn_point_delegation_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateSpawnPointDelegationBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateSpawnPointDelegationBlockPacket::write, UpdateSpawnPointDelegationBlockPacket::new);

	public UpdateSpawnPointDelegationBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.entranceDelegationBlockPosition);
		registryByteBuf.writeBlockPos(this.delegatedEntrancePositionOffset);
		registryByteBuf.writeDouble(this.delegatedEntranceYaw);
		registryByteBuf.writeDouble(this.delegatedEntrancePitch);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
