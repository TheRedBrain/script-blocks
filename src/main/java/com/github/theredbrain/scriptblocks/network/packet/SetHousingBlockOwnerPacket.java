package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetHousingBlockOwnerPacket(
		BlockPos housingBlockPosition,
		String owner
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SetHousingBlockOwnerPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("set_housing_block_owner"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetHousingBlockOwnerPacket> PACKET_CODEC = StreamCodec.ofMember(SetHousingBlockOwnerPacket::write, SetHousingBlockOwnerPacket::new);

	public SetHousingBlockOwnerPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeUtf(this.owner);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
