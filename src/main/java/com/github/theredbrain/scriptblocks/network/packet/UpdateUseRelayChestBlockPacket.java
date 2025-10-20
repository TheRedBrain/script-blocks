package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

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
) implements CustomPayload {
	public static final Id<UpdateUseRelayChestBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_use_relay_chest_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateUseRelayChestBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateUseRelayChestBlockPacket::write, UpdateUseRelayChestBlockPacket::new);

	public UpdateUseRelayChestBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.useRelayChestBlockPosition);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeString(this.keyItemTag);
		registryByteBuf.writeString(this.lockedMessage);
		registryByteBuf.writeString(this.lockedSound);
		registryByteBuf.writeString(this.unlockedMessage);
		registryByteBuf.writeString(this.unlockedSound);
		registryByteBuf.writeBlockPos(this.relayBlockPositionOffset);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
