package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record TeleportToTeamPacket(Identifier targetWorldIdentifier, BlockPos targetPosition, double targetYaw,
								   double targetPitch) implements CustomPayload {
	public static final CustomPayload.Id<TeleportToTeamPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("teleport_to_team"));
	public static final PacketCodec<RegistryByteBuf, TeleportToTeamPacket> PACKET_CODEC = PacketCodec.of(TeleportToTeamPacket::write, TeleportToTeamPacket::new);

	public TeleportToTeamPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readIdentifier(), registryByteBuf.readBlockPos(), registryByteBuf.readDouble(), registryByteBuf.readDouble());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeIdentifier(this.targetWorldIdentifier);
		registryByteBuf.writeBlockPos(this.targetPosition);
		registryByteBuf.writeDouble(this.targetYaw);
		registryByteBuf.writeDouble(this.targetPitch);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
