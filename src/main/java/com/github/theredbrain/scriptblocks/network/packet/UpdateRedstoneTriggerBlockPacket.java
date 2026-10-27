package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateRedstoneTriggerBlockPacket(
		BlockPos redstoneTriggerBlockPosition,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateRedstoneTriggerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_redstone_trigger_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateRedstoneTriggerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateRedstoneTriggerBlockPacket::write, UpdateRedstoneTriggerBlockPacket::new);

	public UpdateRedstoneTriggerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.redstoneTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
