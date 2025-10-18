package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateInteractiveTriggerBlockPacket(
		BlockPos interactiveTriggerBlockPosition,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		String keyItemTag,
		String lockedMessage,
		String lockedSound,
		String unlockedMessage,
		String unlockedSound
) implements CustomPayload {
	public static final Id<UpdateInteractiveTriggerBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_interactive_trigger_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateInteractiveTriggerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateInteractiveTriggerBlockPacket::write, UpdateInteractiveTriggerBlockPacket::new);

	public UpdateInteractiveTriggerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.interactiveTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeString(this.keyItemTag);
		registryByteBuf.writeString(this.lockedMessage);
		registryByteBuf.writeString(this.lockedSound);
		registryByteBuf.writeString(this.unlockedMessage);
		registryByteBuf.writeString(this.unlockedSound);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
