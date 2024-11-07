package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

import static com.github.theredbrain.scriptblocks.util.CustomPacketCodecs.MUTABLE_PAIR_STRING_STRING;

public record UpdateDataSavingBlockPacket(
		BlockPos dataSavingBlockPosition,
		List<MutablePair<String, String>> dataList
) implements CustomPayload {
	public static final Id<UpdateDataSavingBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_data_saving_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDataSavingBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDataSavingBlockPacket::write, UpdateDataSavingBlockPacket::new);

	public UpdateDataSavingBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(MUTABLE_PAIR_STRING_STRING)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataSavingBlockPosition);
		registryByteBuf.writeCollection(this.dataList, MUTABLE_PAIR_STRING_STRING);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
