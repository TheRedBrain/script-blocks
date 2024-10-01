package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateRedstoneTriggerBlockPacket(BlockPos redstoneTriggerBlockPosition,
											   BlockPos triggeredBlockPositionOffset,
											   boolean triggeredBlockResets) implements CustomPayload {
	public static final CustomPayload.Id<UpdateRedstoneTriggerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_redstone_trigger_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateRedstoneTriggerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateRedstoneTriggerBlockPacket::write, UpdateRedstoneTriggerBlockPacket::new);

	public UpdateRedstoneTriggerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos(), registryByteBuf.readBoolean());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.redstoneTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
