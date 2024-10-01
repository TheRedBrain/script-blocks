package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateBossControllerBlockPacket(BlockPos bossControllerBlockPosition, boolean showArea,
											  Vec3i applicationAreaDimensions, BlockPos applicationAreaPositionOffset,
											  String bossIdentifier, BlockPos entitySpawnPositionOffset,
											  double entitySpawnOrientationPitch, double entitySpawnOrientationYaw,
											  List<MutablePair<String, MutablePair<BlockPos, Boolean>>> bossTriggeredBlocksList) implements CustomPayload {
	public static final CustomPayload.Id<UpdateBossControllerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_boss_controller_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateBossControllerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateBossControllerBlockPacket::write, UpdateBossControllerBlockPacket::new);

	public UpdateBossControllerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.bossControllerBlockPosition);

		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.applicationAreaDimensions.getX());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getY());
		registryByteBuf.writeInt(this.applicationAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.applicationAreaPositionOffset);

		registryByteBuf.writeString(this.bossIdentifier);
		registryByteBuf.writeBlockPos(this.entitySpawnPositionOffset);
		registryByteBuf.writeDouble(this.entitySpawnOrientationPitch);
		registryByteBuf.writeDouble(this.entitySpawnOrientationYaw);

		registryByteBuf.writeCollection(this.bossTriggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
