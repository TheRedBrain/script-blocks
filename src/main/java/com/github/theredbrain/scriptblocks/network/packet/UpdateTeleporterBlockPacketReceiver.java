package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateTeleporterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTeleporterBlockPacket> {
	@Override
	public void receive(UpdateTeleporterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity player = context.player();

		if (!player.isCreativeLevelTwoOp()) {
			return;
		}

		BlockPos teleportBlockPosition = payload.teleportBlockPosition();

		boolean showActivationArea = payload.showActivationArea();
		boolean showAdventureScreen = payload.showAdventureScreen();

		Vec3i activationAreaDimensions = payload.activationAreaDimensions();
		BlockPos activationAreaPositionOffset = payload.activationAreaPositionOffset();

		BlockPos accessPositionOffset = payload.accessPositionOffset();
		boolean setAccessPosition = payload.setAccessPosition();

		List<String> statusEffectsToDecrementLevelOnTeleport = payload.statusEffectsToDecrementLevelOnTeleport();

		boolean onlyTeleportDimensionOwner = payload.onlyTeleportDimensionOwner();
		boolean teleportTeam = payload.teleportTeam();

		TeleporterBlockEntity.TeleportationMode teleportationMode = TeleporterBlockEntity.TeleportationMode.byName(payload.teleportationMode()).orElse(TeleporterBlockEntity.TeleportationMode.DIRECT);

		BlockPos directTeleportPositionOffset = payload.directTeleportPositionOffset();
		double directTeleportOrientationYaw = payload.directTeleportOrientationYaw();
		double directTeleportOrientationPitch = payload.directTeleportOrientationPitch();

		TeleporterBlockEntity.SpawnPointType spawnPointType = TeleporterBlockEntity.SpawnPointType.valueOf(payload.spawnPointType());

		List<MutablePair<MutablePair<String, String>, MutablePair<String, Integer>>> locationsList = payload.locationsList();

		String teleporterName = payload.teleporterName();
		String currentTargetIdentifierLabel = payload.currentTargetIdentifierLabel();
		String currentTargetOwnerLabel = payload.currentTargetOwnerLabel();
		boolean showRegenerateButton = payload.showRegenerateButton();
		String teleportButtonLabel = payload.teleportButtonLabel();
		String cancelTeleportButtonLabel = payload.cancelTeleportButtonLabel();

		World world = player.getWorld();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(teleportBlockPosition);
		BlockState blockState = world.getBlockState(teleportBlockPosition);

		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			teleporterBlockEntity.setShowAdventureScreen(showAdventureScreen);
			teleporterBlockEntity.setShowActivationArea(showActivationArea);
			teleporterBlockEntity.setActivationAreaDimensions(activationAreaDimensions);
			teleporterBlockEntity.setActivationAreaPositionOffset(activationAreaPositionOffset);
			teleporterBlockEntity.setAccessPositionOffset(accessPositionOffset);
			teleporterBlockEntity.setSetAccessPosition(setAccessPosition);
			teleporterBlockEntity.setStatusEffectsToDecrementLevelOnTeleport(statusEffectsToDecrementLevelOnTeleport);
			teleporterBlockEntity.setOnlyTeleportDimensionOwner(onlyTeleportDimensionOwner);
			teleporterBlockEntity.setTeleportTeam(teleportTeam);
			teleporterBlockEntity.setTeleportationMode(teleportationMode);
			if (teleportationMode == TeleporterBlockEntity.TeleportationMode.DIRECT) {
				teleporterBlockEntity.setDirectTeleportPositionOffset(directTeleportPositionOffset);
				if (!teleporterBlockEntity.setDirectTeleportOrientationYaw(directTeleportOrientationYaw)) {
					player.sendMessage(Text.translatable("teleporter_block.directTeleportOrientationYaw.invalid"), false);
					updateSuccessful = false;
				}
				if (!teleporterBlockEntity.setDirectTeleportOrientationPitch(directTeleportOrientationPitch)) {
					player.sendMessage(Text.translatable("teleporter_block.directTeleportOrientationPitch.invalid"), false);
					updateSuccessful = false;
				}
			} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.SPAWN_POINTS) {
				teleporterBlockEntity.setSpawnPointType(spawnPointType);
			} else if (teleportationMode == TeleporterBlockEntity.TeleportationMode.LOCATIONS) {
				if (!teleporterBlockEntity.setLocationsList(locationsList)) {
					player.sendMessage(Text.translatable("teleporter_block.locationsList.invalid"), false);
					updateSuccessful = false;
				}
			}
			teleporterBlockEntity.setTeleporterName(teleporterName);
			teleporterBlockEntity.setCurrentTargetIdentifierLabel(currentTargetIdentifierLabel);
			teleporterBlockEntity.setCurrentTargetOwnerLabel(currentTargetOwnerLabel);
			teleporterBlockEntity.setShowRegenerateButton(showRegenerateButton);
			teleporterBlockEntity.setTeleportButtonLabel(teleportButtonLabel);
			teleporterBlockEntity.setCancelTeleportButtonLabel(cancelTeleportButtonLabel);

			if (updateSuccessful) {
				player.sendMessage(Text.translatable("hud.message.script_block.update_successful"), true);
			}
			teleporterBlockEntity.markDirty();
			world.updateListeners(teleportBlockPosition, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
