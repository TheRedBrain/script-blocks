package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

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
) implements CustomPayload {
	public static final Id<UpdateTriggeredRNGBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggered_rng_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredRNGBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredRNGBlockPacket::write, UpdateTriggeredRNGBlockPacket::new);

	public UpdateTriggeredRNGBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredRNGBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeString(this.overrideDataIdentifier);
		registryByteBuf.writeString(this.overrideDataValue);
		registryByteBuf.writeBlockPos(this.overrideTriggeredBlockPosOffset);
		registryByteBuf.writeBoolean(this.overrideTriggeredBlockResets);
		registryByteBuf.writeString(this.influencingAttributeIdentifierString);
		registryByteBuf.writeBoolean(this.checksTeamAttributes);
		registryByteBuf.writeBoolean(this.isAffectedByLuck);
		registryByteBuf.writeInt(this.randomMinValue);
		registryByteBuf.writeInt(this.randomMaxValue);
		registryByteBuf.writeBlockPos(this.fallbackTriggeredBlockPosOffset);
		registryByteBuf.writeBoolean(this.fallbackTriggeredBlockResets);
		registryByteBuf.writeCollection(this.triggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
