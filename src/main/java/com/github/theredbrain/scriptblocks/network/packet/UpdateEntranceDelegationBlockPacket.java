package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateEntranceDelegationBlockPacket(BlockPos entranceDelegationBlockPosition,
												  BlockPos delegatedEntrancePositionOffset, double delegatedEntranceYaw,
												  double delegatedEntrancePitch) implements CustomPayload {
	public static final CustomPayload.Id<UpdateEntranceDelegationBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_entrance_delegation_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateEntranceDelegationBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateEntranceDelegationBlockPacket::write, UpdateEntranceDelegationBlockPacket::new);

	public UpdateEntranceDelegationBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos(), registryByteBuf.readDouble(), registryByteBuf.readDouble());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.entranceDelegationBlockPosition);
		registryByteBuf.writeBlockPos(this.delegatedEntrancePositionOffset);
		registryByteBuf.writeDouble(this.delegatedEntranceYaw);
		registryByteBuf.writeDouble(this.delegatedEntrancePitch);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
