package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateUseRelayChestBlockPacket(
		BlockPos useRelayChestBlockPosition,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		String keyItemTag,
		String lockedMessage,
		String lockedSound,
		String unlockedMessage,
		String unlockedSound,
		BlockPos relayBlockPositionOffset
) implements CustomPacketPayload {
	public static final Type<UpdateUseRelayChestBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_use_relay_chest_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateUseRelayChestBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateUseRelayChestBlockPacket::write, UpdateUseRelayChestBlockPacket::new);

	public UpdateUseRelayChestBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.useRelayChestBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeUtf(this.keyItemTag);
		registryByteBuf.writeUtf(this.lockedMessage);
		registryByteBuf.writeUtf(this.lockedSound);
		registryByteBuf.writeUtf(this.unlockedMessage);
		registryByteBuf.writeUtf(this.unlockedSound);
		registryByteBuf.writeBlockPos(this.relayBlockPositionOffset);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
