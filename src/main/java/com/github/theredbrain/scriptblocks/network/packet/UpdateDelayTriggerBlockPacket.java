package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateDelayTriggerBlockPacket(
		BlockPos delayTriggerBlockPosition,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		int triggerDelay
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateDelayTriggerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_delay_trigger_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDelayTriggerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDelayTriggerBlockPacket::write, UpdateDelayTriggerBlockPacket::new);

	public UpdateDelayTriggerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.delayTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeInt(this.triggerDelay);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
