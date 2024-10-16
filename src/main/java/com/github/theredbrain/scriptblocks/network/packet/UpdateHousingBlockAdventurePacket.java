package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record UpdateHousingBlockAdventurePacket(
		BlockPos housingBlockPosition,
		List<String> coOwnerList,
		List<String> trustedList,
		List<String> guestList
) implements CustomPayload {
	public static final CustomPayload.Id<UpdateHousingBlockAdventurePacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_housing_block_adventure"));
	public static final PacketCodec<RegistryByteBuf, UpdateHousingBlockAdventurePacket> PACKET_CODEC = PacketCodec.of(UpdateHousingBlockAdventurePacket::write, UpdateHousingBlockAdventurePacket::new);

	public UpdateHousingBlockAdventurePacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(PacketCodecs.STRING),
				registryByteBuf.readList(PacketCodecs.STRING),
				registryByteBuf.readList(PacketCodecs.STRING)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeCollection(this.coOwnerList, PacketCodecs.STRING);
		registryByteBuf.writeCollection(this.trustedList, PacketCodecs.STRING);
		registryByteBuf.writeCollection(this.guestList, PacketCodecs.STRING);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
