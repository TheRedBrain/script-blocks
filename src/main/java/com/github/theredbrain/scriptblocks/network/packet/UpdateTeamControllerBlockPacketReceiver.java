package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Team;

public class UpdateTeamControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTeamControllerBlockPacket> {
	@Override
	public void receive(UpdateTeamControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos teamControllerBlockPosition = payload.teamControllerBlockPosition();

		boolean enableTicking = payload.enableTicking();
		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();
		BlockPos pvpControllerBlockPositionOffset = payload.pvpControllerBlockPositionOffset();

		String teamIdentifierString = payload.teamIdentifierString();
		String displayNameString = payload.displayNameString();
		int teamColorIndex = payload.teamColorIndex();
		boolean friendlyFire = payload.friendlyFire();
		boolean showFriendlyInvisibles = payload.showFriendlyInvisibles();
		Team.Visibility nametagVisibility = payload.nametagVisibility();
		Team.Visibility deathMessageVisibility = payload.deathMessageVisibility();
		Team.CollisionRule collisionRule = payload.collisionRule();
		String prefixString = payload.prefixString();
		String suffixString = payload.suffixString();

		ServerLevel serverLevel = serverPlayerEntity.level();

		BlockEntity blockEntity = serverLevel.getBlockEntity(teamControllerBlockPosition);
		BlockState blockState = serverLevel.getBlockState(teamControllerBlockPosition);

		if (blockEntity instanceof TeamControllerBlockEntity teamControllerBlockEntity) {
			teamControllerBlockEntity.reset();
			teamControllerBlockEntity.setEnableTicking(enableTicking);
			teamControllerBlockEntity.setShowArea(showArea);
			teamControllerBlockEntity.setAreaDimensions(areaDimensions);
			teamControllerBlockEntity.setAreaPositionOffset(areaPositionOffset);
			teamControllerBlockEntity.setPVPControllerBlockPositionOffset(pvpControllerBlockPositionOffset);
			teamControllerBlockEntity.setTeamIdentifier(teamIdentifierString);
			teamControllerBlockEntity.setDisplayNameString(displayNameString);
			teamControllerBlockEntity.setTeamColor(teamColorIndex);
			teamControllerBlockEntity.setFriendlyFire(friendlyFire);
			teamControllerBlockEntity.setShowFriendlyInvisibles(showFriendlyInvisibles);
			teamControllerBlockEntity.setNametagVisibility(nametagVisibility);
			teamControllerBlockEntity.setDeathMessageVisibility(deathMessageVisibility);
			teamControllerBlockEntity.setCollisionRule(collisionRule);
			teamControllerBlockEntity.setPrefixString(prefixString);
			teamControllerBlockEntity.setSuffixString(suffixString);

			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			teamControllerBlockEntity.setChanged();
			serverLevel.sendBlockUpdated(teamControllerBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
