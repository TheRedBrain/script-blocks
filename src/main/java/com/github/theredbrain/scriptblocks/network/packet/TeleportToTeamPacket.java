package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TeleportToTeamPacket(
		Identifier targetWorldIdentifier,
		BlockPos targetPosition,
		double targetYaw,
		double targetPitch
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<TeleportToTeamPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("teleport_to_team"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TeleportToTeamPacket> PACKET_CODEC = StreamCodec.ofMember(TeleportToTeamPacket::write, TeleportToTeamPacket::new);

	public TeleportToTeamPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readIdentifier(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readDouble(),
				registryByteBuf.readDouble()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.targetWorldIdentifier);
		registryByteBuf.writeBlockPos(this.targetPosition);
		registryByteBuf.writeDouble(this.targetYaw);
		registryByteBuf.writeDouble(this.targetPitch);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
