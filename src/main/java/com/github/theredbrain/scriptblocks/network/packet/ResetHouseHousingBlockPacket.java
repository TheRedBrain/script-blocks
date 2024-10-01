package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ResetHouseHousingBlockPacket(BlockPos housingBlockPosition) implements CustomPayload {
	public static final CustomPayload.Id<ResetHouseHousingBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("reset_house_housing_block"));
	public static final PacketCodec<RegistryByteBuf, ResetHouseHousingBlockPacket> PACKET_CODEC = PacketCodec.of(ResetHouseHousingBlockPacket::write, ResetHouseHousingBlockPacket::new);

	public ResetHouseHousingBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
