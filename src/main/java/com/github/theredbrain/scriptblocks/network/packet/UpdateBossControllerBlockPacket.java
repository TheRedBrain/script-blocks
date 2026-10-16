package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateBossControllerBlockPacket(
		BlockPos bossControllerBlockPosition,
		BlockPos uuidListHandlerBlockPositionOffset,
		BlockPos noPlayersAroundTriggeredBlockPositionOffset,
		boolean noPlayersAroundTriggeredBlockResets,
		String bossIdentifier,
		BlockPos entitySpawnPositionOffset,
		double entitySpawnOrientationPitch,
		double entitySpawnOrientationYaw,
		boolean isPersistent,
		boolean enableSpawnEffects,
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList
) implements CustomPayload {
	public static final CustomPayload.Id<UpdateBossControllerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_boss_controller_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateBossControllerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateBossControllerBlockPacket::write, UpdateBossControllerBlockPacket::new);

	public UpdateBossControllerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.bossControllerBlockPosition);

		registryByteBuf.writeBlockPos(this.uuidListHandlerBlockPositionOffset);

		registryByteBuf.writeBlockPos(this.noPlayersAroundTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.noPlayersAroundTriggeredBlockResets);

		registryByteBuf.writeString(this.bossIdentifier);
		registryByteBuf.writeBlockPos(this.entitySpawnPositionOffset);
		registryByteBuf.writeDouble(this.entitySpawnOrientationPitch);
		registryByteBuf.writeDouble(this.entitySpawnOrientationYaw);

		registryByteBuf.writeBoolean(this.isPersistent);
		registryByteBuf.writeBoolean(this.enableSpawnEffects);

		registryByteBuf.writeCollection(this.bossTriggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
