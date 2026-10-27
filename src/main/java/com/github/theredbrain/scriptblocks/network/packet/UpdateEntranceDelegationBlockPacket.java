package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Deprecated
public record UpdateEntranceDelegationBlockPacket(
		BlockPos entranceDelegationBlockPosition,
		BlockPos delegatedEntrancePositionOffset,
		double delegatedEntranceYaw,
		double delegatedEntrancePitch
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateEntranceDelegationBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_entrance_delegation_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEntranceDelegationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateEntranceDelegationBlockPacket::write, UpdateEntranceDelegationBlockPacket::new);

	public UpdateEntranceDelegationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.entranceDelegationBlockPosition);
		registryByteBuf.writeBlockPos(this.delegatedEntrancePositionOffset);
		registryByteBuf.writeDouble(this.delegatedEntranceYaw);
		registryByteBuf.writeDouble(this.delegatedEntrancePitch);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
