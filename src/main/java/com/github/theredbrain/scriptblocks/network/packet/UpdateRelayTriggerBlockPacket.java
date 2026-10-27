package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateRelayTriggerBlockPacket(
		BlockPos relayTriggerBlockPosition,
		String selectionMode,
		boolean showArea,
		boolean resetsArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks,
		String triggerMode,
		boolean isTriggerAmountDataDriven,
		BlockPos dataProvidingBlockPosOffset,
		String dataIdentifier,
		int triggerAmount
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateRelayTriggerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_relay_trigger_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRelayTriggerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateRelayTriggerBlockPacket::write, UpdateRelayTriggerBlockPacket::new);

	public UpdateRelayTriggerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt());
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.relayTriggerBlockPosition);
		registryByteBuf.writeUtf(this.selectionMode);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeBoolean(this.resetsArea);

		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);

		registryByteBuf.writeCollection(this.triggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER);
		registryByteBuf.writeUtf(this.triggerMode);
		registryByteBuf.writeBoolean(this.isTriggerAmountDataDriven);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeUtf(this.dataIdentifier);
		registryByteBuf.writeInt(this.triggerAmount);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
