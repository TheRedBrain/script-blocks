package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;

@Deprecated
public record UpdateEntranceDelegationBlockPacket(
		BlockPos entranceDelegationBlockPosition,
		LevelData.RespawnData delegatedEntrance
		) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateEntranceDelegationBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_entrance_delegation_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEntranceDelegationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateEntranceDelegationBlockPacket::write, UpdateEntranceDelegationBlockPacket::new);

	public UpdateEntranceDelegationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf)
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.entranceDelegationBlockPosition);
		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.delegatedEntrance);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
