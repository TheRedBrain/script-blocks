package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateSpawnPointDelegationBlockPacket(
		BlockPos spawnPointDelegationBlockPosition,
		List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints
) implements CustomPacketPayload {
	public static final Type<UpdateSpawnPointDelegationBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_spawn_point_delegation_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateSpawnPointDelegationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateSpawnPointDelegationBlockPacket::write, UpdateSpawnPointDelegationBlockPacket::new);

	public UpdateSpawnPointDelegationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.spawnPointDelegationBlockPosition);
		registryByteBuf.writeCollection(this.delegatedSpawnPoints, CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_DOUBLE_DOUBLE);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
