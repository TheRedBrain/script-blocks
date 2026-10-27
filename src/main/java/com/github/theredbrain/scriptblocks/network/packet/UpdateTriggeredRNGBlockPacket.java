package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeredRNGBlockPacket(
		BlockPos triggeredRNGBlockPosition,
		BlockPos dataProvidingBlockPosOffset,
		String overrideDataIdentifier,
		String overrideDataValue,
		BlockPos overrideTriggeredBlockPosOffset,
		boolean overrideTriggeredBlockResets,
		String influencingAttributeIdentifierString,
		boolean checksTeamAttributes,
		boolean isAffectedByLuck,
		int randomMinValue,
		int randomMaxValue,
		BlockPos fallbackTriggeredBlockPosOffset,
		boolean fallbackTriggeredBlockResets,
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredRNGBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_rng_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredRNGBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredRNGBlockPacket::write, UpdateTriggeredRNGBlockPacket::new);

	public UpdateTriggeredRNGBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredRNGBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeUtf(this.overrideDataIdentifier);
		registryByteBuf.writeUtf(this.overrideDataValue);
		registryByteBuf.writeBlockPos(this.overrideTriggeredBlockPosOffset);
		registryByteBuf.writeBoolean(this.overrideTriggeredBlockResets);
		registryByteBuf.writeUtf(this.influencingAttributeIdentifierString);
		registryByteBuf.writeBoolean(this.checksTeamAttributes);
		registryByteBuf.writeBoolean(this.isAffectedByLuck);
		registryByteBuf.writeInt(this.randomMinValue);
		registryByteBuf.writeInt(this.randomMaxValue);
		registryByteBuf.writeBlockPos(this.fallbackTriggeredBlockPosOffset);
		registryByteBuf.writeBoolean(this.fallbackTriggeredBlockResets);
		registryByteBuf.writeCollection(this.triggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
