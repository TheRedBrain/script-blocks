package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateMimicBlockPacket(BlockPos mimicBlockPosition, BlockPos activeMimicBlockPositionOffset,
									 BlockPos inactiveMimicBlockPositionOffset) implements CustomPayload {
	public static final CustomPayload.Id<UpdateMimicBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_mimic_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateMimicBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateMimicBlockPacket::write, UpdateMimicBlockPacket::new);

	public UpdateMimicBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.mimicBlockPosition);
		registryByteBuf.writeBlockPos(this.activeMimicBlockPositionOffset);
		registryByteBuf.writeBlockPos(this.inactiveMimicBlockPositionOffset);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
