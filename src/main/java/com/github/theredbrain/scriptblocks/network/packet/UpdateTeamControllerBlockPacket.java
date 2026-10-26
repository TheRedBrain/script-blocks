package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateTeamControllerBlockPacket(
		BlockPos teamControllerBlockPosition,
		boolean enableTicking,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		BlockPos pvpControllerBlockPositionOffset,
		String teamIdentifierString,
		String displayNameString,
		int teamColorIndex,
		boolean friendlyFire,
		boolean showFriendlyInvisibles,
		AbstractTeam.VisibilityRule nametagVisibility,
		AbstractTeam.VisibilityRule deathMessageVisibility,
		AbstractTeam.CollisionRule collisionRule,
		String prefixString,
		String suffixString
) implements CustomPayload {
	public static final Id<UpdateTeamControllerBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_team_controller_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTeamControllerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTeamControllerBlockPacket::write, UpdateTeamControllerBlockPacket::new);

	public UpdateTeamControllerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				AbstractTeam.VisibilityRule.PACKET_CODEC.decode(registryByteBuf),
				AbstractTeam.VisibilityRule.PACKET_CODEC.decode(registryByteBuf),
				AbstractTeam.CollisionRule.PACKET_CODEC.decode(registryByteBuf),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teamControllerBlockPosition);
		registryByteBuf.writeBoolean(this.enableTicking);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeBlockPos(this.pvpControllerBlockPositionOffset);
		registryByteBuf.writeString(this.teamIdentifierString);
		registryByteBuf.writeString(this.displayNameString);
		registryByteBuf.writeInt(this.teamColorIndex);
		registryByteBuf.writeBoolean(this.friendlyFire);
		registryByteBuf.writeBoolean(this.showFriendlyInvisibles);
		AbstractTeam.VisibilityRule.PACKET_CODEC.encode(registryByteBuf, this.nametagVisibility);
		AbstractTeam.VisibilityRule.PACKET_CODEC.encode(registryByteBuf, this.deathMessageVisibility);
		AbstractTeam.CollisionRule.PACKET_CODEC.encode(registryByteBuf, this.collisionRule);
		registryByteBuf.writeString(this.prefixString);
		registryByteBuf.writeString(this.suffixString);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
