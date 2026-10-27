package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.github.theredbrain.scriptblocks.util.CustomPacketCodecs.MUTABLE_PAIR_STRING_STRING;

public record UpdateDataSavingBlockPacket(
		BlockPos dataSavingBlockPosition,
		List<MutablePair<String, String>> dataList
) implements CustomPacketPayload {
	public static final Type<UpdateDataSavingBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_data_saving_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDataSavingBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDataSavingBlockPacket::write, UpdateDataSavingBlockPacket::new);

	public UpdateDataSavingBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(MUTABLE_PAIR_STRING_STRING)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataSavingBlockPosition);
		registryByteBuf.writeCollection(this.dataList, MUTABLE_PAIR_STRING_STRING);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
