package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;

import java.util.List;

public record UpdateSpawnPointDelegationBlockPacket(
		BlockPos spawnPointDelegationBlockPosition,
		List<LevelData.RespawnData> delegatedSpawnPoints
) implements CustomPacketPayload {
	public static final Type<UpdateSpawnPointDelegationBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_spawn_point_delegation_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSpawnPointDelegationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateSpawnPointDelegationBlockPacket::write, UpdateSpawnPointDelegationBlockPacket::new);

	public UpdateSpawnPointDelegationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(LevelData.RespawnData.STREAM_CODEC)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.spawnPointDelegationBlockPosition);
		registryByteBuf.writeCollection(this.delegatedSpawnPoints, LevelData.RespawnData.STREAM_CODEC);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
