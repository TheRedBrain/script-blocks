package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.scores.Team;

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
		Team.Visibility nametagVisibility,
		Team.Visibility deathMessageVisibility,
		Team.CollisionRule collisionRule,
		String prefixString,
		String suffixString
) implements CustomPacketPayload {
	public static final Type<UpdateTeamControllerBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_team_controller_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTeamControllerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTeamControllerBlockPacket::write, UpdateTeamControllerBlockPacket::new);

	public UpdateTeamControllerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
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
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				Team.Visibility.STREAM_CODEC.decode(registryByteBuf),
				Team.Visibility.STREAM_CODEC.decode(registryByteBuf),
				Team.CollisionRule.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teamControllerBlockPosition);
		registryByteBuf.writeBoolean(this.enableTicking);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeBlockPos(this.pvpControllerBlockPositionOffset);
		registryByteBuf.writeUtf(this.teamIdentifierString);
		registryByteBuf.writeUtf(this.displayNameString);
		registryByteBuf.writeInt(this.teamColorIndex);
		registryByteBuf.writeBoolean(this.friendlyFire);
		registryByteBuf.writeBoolean(this.showFriendlyInvisibles);
		Team.Visibility.STREAM_CODEC.encode(registryByteBuf, this.nametagVisibility);
		Team.Visibility.STREAM_CODEC.encode(registryByteBuf, this.deathMessageVisibility);
		Team.CollisionRule.STREAM_CODEC.encode(registryByteBuf, this.collisionRule);
		registryByteBuf.writeUtf(this.prefixString);
		registryByteBuf.writeUtf(this.suffixString);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
