package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record LeaveHouseFromHousingScreenPacket() implements CustomPayload {
	public static final CustomPayload.Id<LeaveHouseFromHousingScreenPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("leave_house_from_housing_screen"));
	public static final PacketCodec<RegistryByteBuf, LeaveHouseFromHousingScreenPacket> PACKET_CODEC = PacketCodec.of(LeaveHouseFromHousingScreenPacket::write, LeaveHouseFromHousingScreenPacket::new);

	public LeaveHouseFromHousingScreenPacket(RegistryByteBuf registryByteBuf) {
		this();
	}

	private void write(RegistryByteBuf registryByteBuf) {
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
