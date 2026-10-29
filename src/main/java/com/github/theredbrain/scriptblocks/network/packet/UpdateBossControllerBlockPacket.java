package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateBossControllerBlockPacket(
		BlockPos bossControllerBlockPosition,
		BlockPos uuidListHandlerBlockPositionOffset,
		BlockPos noPlayersAroundTriggeredBlockPositionOffset,
		boolean noPlayersAroundTriggeredBlockResets,
		String bossIdentifier,
		LevelData.RespawnData spawnPoint,
		boolean isPersistent,
		boolean enableSpawnEffects,
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateBossControllerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_boss_controller_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateBossControllerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateBossControllerBlockPacket::write, UpdateBossControllerBlockPacket::new);

	public UpdateBossControllerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.bossControllerBlockPosition);

		registryByteBuf.writeBlockPos(this.uuidListHandlerBlockPositionOffset);

		registryByteBuf.writeBlockPos(this.noPlayersAroundTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.noPlayersAroundTriggeredBlockResets);

		registryByteBuf.writeUtf(this.bossIdentifier);
		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.spawnPoint);

		registryByteBuf.writeBoolean(this.isPersistent);
		registryByteBuf.writeBoolean(this.enableSpawnEffects);

		registryByteBuf.writeCollection(this.bossTriggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
