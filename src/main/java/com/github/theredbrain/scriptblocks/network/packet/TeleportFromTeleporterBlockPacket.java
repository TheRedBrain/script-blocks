package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record TeleportFromTeleporterBlockPacket(
		BlockPos teleportBlockPosition,
		String targetDimensionOwnerName,
		String targetLocation,
		String targetLocationEntrance,
		String dataId,
		String data
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<TeleportFromTeleporterBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("teleport_from_teleporter_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TeleportFromTeleporterBlockPacket> PACKET_CODEC = StreamCodec.ofMember(TeleportFromTeleporterBlockPacket::write, TeleportFromTeleporterBlockPacket::new);

	public TeleportFromTeleporterBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teleportBlockPosition);
		registryByteBuf.writeUtf(this.targetDimensionOwnerName);
		registryByteBuf.writeUtf(this.targetLocation);
		registryByteBuf.writeUtf(this.targetLocationEntrance);
		registryByteBuf.writeUtf(this.dataId);
		registryByteBuf.writeUtf(this.data);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
