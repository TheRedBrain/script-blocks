package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateHousingBlockCreativePacket(
		BlockPos housingBlockPosition,
		boolean showRestrictBlockBreakingArea,
		Vec3i restrictBlockBreakingAreaDimensions,
		BlockPos restrictBlockBreakingAreaPositionOffset,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		String ownerMode
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateHousingBlockCreativePacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_housing_block_creative"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHousingBlockCreativePacket> PACKET_CODEC = StreamCodec.ofMember(UpdateHousingBlockCreativePacket::write, UpdateHousingBlockCreativePacket::new);

	public UpdateHousingBlockCreativePacket(RegistryFriendlyByteBuf registryByteBuf) {
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
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeBoolean(this.showRestrictBlockBreakingArea);
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getX());
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getY());
		registryByteBuf.writeInt(this.restrictBlockBreakingAreaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.restrictBlockBreakingAreaPositionOffset);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeUtf(this.ownerMode);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
