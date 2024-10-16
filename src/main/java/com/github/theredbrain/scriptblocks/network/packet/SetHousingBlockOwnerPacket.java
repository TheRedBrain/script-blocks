package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetHousingBlockOwnerPacket(BlockPos housingBlockPosition, String owner) implements CustomPayload {
	public static final CustomPayload.Id<SetHousingBlockOwnerPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("set_housing_block_owner"));
	public static final PacketCodec<RegistryByteBuf, SetHousingBlockOwnerPacket> PACKET_CODEC = PacketCodec.of(SetHousingBlockOwnerPacket::write, SetHousingBlockOwnerPacket::new);

	public SetHousingBlockOwnerPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readString());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeString(this.owner);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
