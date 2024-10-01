package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateHousingBlockCreativePacket(BlockPos housingBlockPosition, boolean showRestrictBlockBreakingArea,
											   Vec3i restrictBlockBreakingAreaDimensions,
											   BlockPos restrictBlockBreakingAreaPositionOffset,
											   BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets,
											   String ownerMode) implements CustomPayload {
	public static final CustomPayload.Id<UpdateHousingBlockCreativePacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_housing_block_creative"));
	public static final PacketCodec<RegistryByteBuf, UpdateHousingBlockCreativePacket> PACKET_CODEC = PacketCodec.of(UpdateHousingBlockCreativePacket::write, UpdateHousingBlockCreativePacket::new);

	public UpdateHousingBlockCreativePacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeBoolean(this.showRestrictBlockBreakingArea);
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getX());
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getY());
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.restrictBlockBreakingAreaPositionOffset);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeString(this.ownerMode);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
