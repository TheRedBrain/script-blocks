package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LeaveHouseFromHousingScreenPacket() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<LeaveHouseFromHousingScreenPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("leave_house_from_housing_screen"));
	public static final StreamCodec<RegistryFriendlyByteBuf, LeaveHouseFromHousingScreenPacket> PACKET_CODEC = StreamCodec.ofMember(LeaveHouseFromHousingScreenPacket::write, LeaveHouseFromHousingScreenPacket::new);

	public LeaveHouseFromHousingScreenPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this();
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
