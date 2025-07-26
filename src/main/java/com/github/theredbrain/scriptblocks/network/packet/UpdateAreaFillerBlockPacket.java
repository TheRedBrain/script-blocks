package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateAreaFillerBlockPacket(
		BlockPos areaFillerBlockPosition,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		String blockIdentifier
) implements CustomPayload {
	public static final Id<UpdateAreaFillerBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_area_filler_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateAreaFillerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateAreaFillerBlockPacket::write, UpdateAreaFillerBlockPacket::new);

	public UpdateAreaFillerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.areaFillerBlockPosition);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeString(this.blockIdentifier);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
