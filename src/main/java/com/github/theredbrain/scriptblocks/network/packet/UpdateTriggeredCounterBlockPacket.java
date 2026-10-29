package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTriggeredCounterBlockPacket(
		BlockPos triggeredCounterBlockPosition,
		List<MutablePair<Integer, MutablePair<BlockPos, Boolean>>> triggeredBlocksList
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateTriggeredCounterBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_triggered_counter_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredCounterBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredCounterBlockPacket::write, UpdateTriggeredCounterBlockPacket::new);

	public UpdateTriggeredCounterBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredCounterBlockPosition);
		registryByteBuf.writeCollection(this.triggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_INTEGER_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
