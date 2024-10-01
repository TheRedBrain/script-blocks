package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTriggeredCounterBlockPacket(BlockPos triggeredCounterBlockPosition,
												List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList) implements CustomPayload {
	public static final CustomPayload.Id<UpdateTriggeredCounterBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_triggered_counter_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredCounterBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredCounterBlockPacket::write, UpdateTriggeredCounterBlockPacket::new);

	public UpdateTriggeredCounterBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN));
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredCounterBlockPosition);
		registryByteBuf.writeCollection(this.triggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
