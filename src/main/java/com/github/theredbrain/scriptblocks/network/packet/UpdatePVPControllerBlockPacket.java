package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdatePVPControllerBlockPacket(
		BlockPos pvpControllerBlockPosition,
		String pvpArenaSettingsIdentifier,
		List<MutablePair<String, LevelData.RespawnData>> sideEntrancesList,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos dataProvidingBlockPositionOffset,
		String matchDurationDataIdentifier
) implements CustomPacketPayload {
	public static final Type<UpdatePVPControllerBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_pvp_controller_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePVPControllerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdatePVPControllerBlockPacket::write, UpdatePVPControllerBlockPacket::new);

	public UpdatePVPControllerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_RESPAWN_DATA),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.pvpControllerBlockPosition);
		registryByteBuf.writeUtf(this.pvpArenaSettingsIdentifier);
		registryByteBuf.writeCollection(this.sideEntrancesList, CustomPacketCodecs.MUTABLE_PAIR_STRING_RESPAWN_DATA);
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeUtf(this.matchDurationDataIdentifier);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
