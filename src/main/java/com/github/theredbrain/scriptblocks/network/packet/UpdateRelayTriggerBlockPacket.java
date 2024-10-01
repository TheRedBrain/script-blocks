package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateRelayTriggerBlockPacket(BlockPos relayTriggerBlockPosition, String selectionMode, boolean showArea,
											boolean resetsArea, Vec3i areaDimensions, BlockPos areaPositionOffset,
											List<MutablePair<MutablePair<BlockPos, Boolean>, Integer>> triggeredBlocks,
											String triggerMode, int triggerAmount) implements CustomPayload {
	public static final CustomPayload.Id<UpdateRelayTriggerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_relay_trigger_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateRelayTriggerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateRelayTriggerBlockPacket::write, UpdateRelayTriggerBlockPacket::new);

	public UpdateRelayTriggerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER),
				registryByteBuf.readString(),
				registryByteBuf.readInt());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.relayTriggerBlockPosition);
		registryByteBuf.writeString(this.selectionMode);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeBoolean(this.resetsArea);

		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);

		registryByteBuf.writeCollection(this.triggeredBlocks, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_BLOCK_POS_BOOLEAN_INTEGER);
		registryByteBuf.writeString(this.triggerMode);
		registryByteBuf.writeInt(this.triggerAmount);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
