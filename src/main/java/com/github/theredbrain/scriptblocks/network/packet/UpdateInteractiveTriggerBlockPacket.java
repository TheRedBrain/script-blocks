package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateInteractiveTriggerBlockPacket(
		BlockPos interactiveTriggerBlockPosition,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		String preventInteractionIdentifierString,
		String keyIdentifierString,
		String interactionPreventedMessage,
		String interactionPreventedSound,
		String lockedMessage,
		String lockedSound,
		String unlockedMessage,
		String unlockedSound
) implements CustomPacketPayload {
	public static final Type<UpdateInteractiveTriggerBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_interactive_trigger_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInteractiveTriggerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateInteractiveTriggerBlockPacket::write, UpdateInteractiveTriggerBlockPacket::new);

	public UpdateInteractiveTriggerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.interactiveTriggerBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeUtf(this.preventInteractionIdentifierString);
		registryByteBuf.writeUtf(this.keyIdentifierString);
		registryByteBuf.writeUtf(this.interactionPreventedMessage);
		registryByteBuf.writeUtf(this.interactionPreventedSound);
		registryByteBuf.writeUtf(this.lockedMessage);
		registryByteBuf.writeUtf(this.lockedSound);
		registryByteBuf.writeUtf(this.unlockedMessage);
		registryByteBuf.writeUtf(this.unlockedSound);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
