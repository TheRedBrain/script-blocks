package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ResetHouseHousingBlockPacket(BlockPos housingBlockPosition) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ResetHouseHousingBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("reset_house_housing_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ResetHouseHousingBlockPacket> PACKET_CODEC = StreamCodec.ofMember(ResetHouseHousingBlockPacket::write, ResetHouseHousingBlockPacket::new);

	public ResetHouseHousingBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos());
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
