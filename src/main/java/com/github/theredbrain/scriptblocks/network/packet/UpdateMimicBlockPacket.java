package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateMimicBlockPacket(
		BlockPos mimicBlockPosition,
		BlockPos activeMimicBlockPositionOffset,
		BlockPos inactiveMimicBlockPositionOffset
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateMimicBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_mimic_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateMimicBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateMimicBlockPacket::write, UpdateMimicBlockPacket::new);

	public UpdateMimicBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.mimicBlockPosition);
		registryByteBuf.writeBlockPos(this.activeMimicBlockPositionOffset);
		registryByteBuf.writeBlockPos(this.inactiveMimicBlockPositionOffset);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
