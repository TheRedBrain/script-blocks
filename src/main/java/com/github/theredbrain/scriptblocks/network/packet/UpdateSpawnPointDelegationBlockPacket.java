package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateSpawnPointDelegationBlockPacket(
		BlockPos spawnPointDelegationBlockPosition,
		List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints
) implements CustomPayload {
	public static final Id<UpdateSpawnPointDelegationBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_spawn_point_delegation_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateSpawnPointDelegationBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateSpawnPointDelegationBlockPacket::write, UpdateSpawnPointDelegationBlockPacket::new);

	public UpdateSpawnPointDelegationBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.spawnPointDelegationBlockPosition);
		registryByteBuf.writeCollection(this.delegatedSpawnPoints, CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
