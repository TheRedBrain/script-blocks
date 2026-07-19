package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class UpdateTeamControllerBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTeamControllerBlockPacket> {
	@Override
	public void receive(UpdateTeamControllerBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		if (!serverPlayerEntity.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos teamControllerBlockPosition = payload.teamControllerBlockPosition();

		boolean showArea = payload.showArea();
		Vec3i areaDimensions = payload.areaDimensions();
		BlockPos areaPositionOffset = payload.areaPositionOffset();
		BlockPos pvpControllerBlockPositionOffset = payload.pvpControllerBlockPositionOffset();

		String teamIdentifierString = payload.teamIdentifierString();
		String displayNameString = payload.displayNameString();
		int teamColorIndex = payload.teamColorIndex();
		boolean friendlyFire = payload.friendlyFire();
		boolean showFriendlyInvisibles = payload.showFriendlyInvisibles();
		String nametagVisibility = payload.nametagVisibilityString();
		String deathMessageVisibility = payload.deathMessageVisibilityString();
		String collisionRule = payload.collisionRuleString();
		String prefixString = payload.prefixString();
		String suffixString = payload.suffixString();

		World world = serverPlayerEntity.getWorld();

		BlockEntity blockEntity = world.getBlockEntity(teamControllerBlockPosition);
		BlockState blockState = world.getBlockState(teamControllerBlockPosition);

		if (blockEntity instanceof TeamControllerBlockEntity teamControllerBlockEntity) {
			teamControllerBlockEntity.reset();
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

			serverPlayerEntity.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			teamControllerBlockEntity.markDirty();
			world.updateListeners(teamControllerBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
