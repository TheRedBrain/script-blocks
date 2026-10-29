package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record UpdateHousingBlockAdventurePacket(
		BlockPos housingBlockPosition,
		List<String> coOwnerList,
		List<String> trustedList,
		List<String> guestList
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateHousingBlockAdventurePacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_housing_block_adventure"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateHousingBlockAdventurePacket> PACKET_CODEC = StreamCodec.ofMember(UpdateHousingBlockAdventurePacket::write, UpdateHousingBlockAdventurePacket::new);

	public UpdateHousingBlockAdventurePacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(ByteBufCodecs.STRING_UTF8),
				registryByteBuf.readList(ByteBufCodecs.STRING_UTF8),
				registryByteBuf.readList(ByteBufCodecs.STRING_UTF8)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.housingBlockPosition);
		registryByteBuf.writeCollection(this.coOwnerList, ByteBufCodecs.STRING_UTF8);
		registryByteBuf.writeCollection(this.trustedList, ByteBufCodecs.STRING_UTF8);
		registryByteBuf.writeCollection(this.guestList, ByteBufCodecs.STRING_UTF8);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
