package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateDelayTriggerBlockPacket(BlockPos delayTriggerBlockPosition, BlockPos triggeredBlockPositionOffset,
											boolean triggeredBlockResets, int triggerDelay) implements CustomPayload {
	public static final CustomPayload.Id<UpdateDelayTriggerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_delay_trigger_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDelayTriggerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDelayTriggerBlockPacket::write, UpdateDelayTriggerBlockPacket::new);

	public UpdateDelayTriggerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos(), registryByteBuf.readBoolean(), registryByteBuf.readInt());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.delayTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeInt(this.triggerDelay);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
