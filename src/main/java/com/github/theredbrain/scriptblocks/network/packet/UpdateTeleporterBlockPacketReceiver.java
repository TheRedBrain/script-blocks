package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateTeleporterBlockPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<UpdateTeleporterBlockPacket> {
	@Override
	public void receive(UpdateTeleporterBlockPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		if (!serverPlayerEntity.canUseGameMasterBlocks()) {
			serverPlayerEntity.sendSystemMessage(Component.translatable("hud.message.script_block.no_permission"));
			return;
		}

		BlockPos teleportBlockPosition = payload.teleportBlockPosition();

		TeleporterBlockEntity.CreativeScreenPage creativeScreenPage = TeleporterBlockEntity.CreativeScreenPage.byName(payload.creativeScreenPage()).orElse(TeleporterBlockEntity.CreativeScreenPage.ACTIVATION);
		boolean showAdventureScreen = payload.showAdventureScreen();

		String preventInteractionIdentifierString = payload.preventInteractionIdentifierString();
		String interactionPreventedMessage = payload.interactionPreventedMessage();

		BlockPos uuidListHandlerBlockPositionOffset = payload.uuidListHandlerBlockPositionOffset();

		BlockPos accessPositionOffset = payload.accessPositionOffset();
		boolean setAccessPosition = payload.setAccessPosition();

		boolean onlyTeleportDimensionOwner = payload.onlyTeleportDimensionOwner();
		String onlyDimensionOwnerCanTeleportMessage = payload.onlyDimensionOwnerCanTeleportMessage();

		boolean teleportTeam = payload.teleportTeam();

		TeleporterBlockEntity.TeleportationMode teleportationMode = TeleporterBlockEntity.TeleportationMode.byName(payload.teleportationMode()).orElse(TeleporterBlockEntity.TeleportationMode.DIRECT);

		BlockPos directTeleportPositionOffset = payload.directTeleportPositionOffset();
		double directTeleportOrientationYaw = payload.directTeleportOrientationYaw();
		double directTeleportOrientationPitch = payload.directTeleportOrientationPitch();

		TeleporterBlockEntity.SpawnPointType spawnPointType = TeleporterBlockEntity.SpawnPointType.byName(payload.spawnPointType()).orElse(TeleporterBlockEntity.SpawnPointType.WORLD_SPAWN);

		List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> locationsList = payload.locationsList();

		MutablePair<MutablePair<String, String>, MutablePair<String, String>> location = payload.location();

		BlockPos dataProvidingBlockPosOffset = payload.dataProvidingBlockPosOffset();
		String locationDataIdentifier = payload.locationDataIdentifier();
		String entranceDataIdentifier = payload.entranceDataIdentifier();
		String sendDataIdentifierDataIdentifier = payload.sendDataIdentifierDataIdentifier();
		String sendDataValueDataIdentifier = payload.sendDataValueDataIdentifier();

		BlockPos preTeleportTriggeredBlockPositionOffset = payload.preTeleportTriggeredBlockPositionOffset();
		boolean preTeleportTriggeredBlockResets = payload.preTeleportTriggeredBlockResets();

		BlockPos onTeleportUUIDListHandlerPositionOffset = payload.onTeleportUUIDListHandlerPositionOffset();

		BlockPos postTeleportTriggeredBlockPositionOffset = payload.postTeleportTriggeredBlockPositionOffset();
		boolean postTeleportTriggeredBlockResets = payload.postTeleportTriggeredBlockResets();

		String teleporterName = payload.teleporterName();
		String currentTargetIdentifierLabel = payload.currentTargetIdentifierLabel();
		String currentTargetOwnerLabel = payload.currentTargetOwnerLabel();
		boolean showCancelButton = payload.showCancelButton();
		boolean canOwnerBeChosen = payload.canOwnerBeChosen();
		String teleportButtonLabel = payload.teleportButtonLabel();
		String cancelTeleportButtonLabel = payload.cancelTeleportButtonLabel();

		Level world = serverPlayerEntity.level();

		boolean updateSuccessful = true;

		BlockEntity blockEntity = world.getBlockEntity(teleportBlockPosition);
		BlockState blockState = world.getBlockState(teleportBlockPosition);

		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			teleporterBlockEntity.setShowAdventureScreen(showAdventureScreen);
			teleporterBlockEntity.setCreativeScreenPage(creativeScreenPage);
			teleporterBlockEntity.setPreventInteractionIdentifierString(preventInteractionIdentifierString);
			teleporterBlockEntity.setInteractionPreventedMessage(interactionPreventedMessage);
			teleporterBlockEntity.setUuidListHandlerPositionOffset(uuidListHandlerBlockPositionOffset);
			teleporterBlockEntity.setAccessPositionOffset(accessPositionOffset);
			teleporterBlockEntity.setSetAccessPosition(setAccessPosition);
			teleporterBlockEntity.setOnlyTeleportDimensionOwner(onlyTeleportDimensionOwner);
			teleporterBlockEntity.setOnlyDimensionOwnerCanTeleportMessage(onlyDimensionOwnerCanTeleportMessage);
			teleporterBlockEntity.setTeleportTeam(teleportTeam);
			teleporterBlockEntity.setTeleportationMode(teleportationMode);
			teleporterBlockEntity.setDirectTeleportPositionOffset(directTeleportPositionOffset);
			if (!teleporterBlockEntity.setDirectTeleportOrientationYaw(directTeleportOrientationYaw)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("teleporter_block.directTeleportOrientationYaw.invalid"), false);
				updateSuccessful = false;
			}
			if (!teleporterBlockEntity.setDirectTeleportOrientationPitch(directTeleportOrientationPitch)) {
				serverPlayerEntity.displayClientMessage(Component.translatable("teleporter_block.directTeleportOrientationPitch.invalid"), false);
				updateSuccessful = false;
			}
			teleporterBlockEntity.setSpawnPointType(spawnPointType);
			teleporterBlockEntity.setLocationsList(locationsList);
			teleporterBlockEntity.setLocation(location);
			teleporterBlockEntity.setDataProvidingBlockPosOffset(dataProvidingBlockPosOffset);
			teleporterBlockEntity.setLocationAppendix(locationDataIdentifier);
			teleporterBlockEntity.setEntranceAppendix(entranceDataIdentifier);
			teleporterBlockEntity.setSentDataIdentifierAppendix(sendDataIdentifierDataIdentifier);
			teleporterBlockEntity.setSentDataValueAppendix(sendDataValueDataIdentifier);
			teleporterBlockEntity.setPreTeleportTriggeredBlock(new MutablePair<>(preTeleportTriggeredBlockPositionOffset, preTeleportTriggeredBlockResets));
			teleporterBlockEntity.setOnTeleportUUIDListHandlerPositionOffset(onTeleportUUIDListHandlerPositionOffset);
			teleporterBlockEntity.setPostTeleportTriggeredBlock(new MutablePair<>(postTeleportTriggeredBlockPositionOffset, postTeleportTriggeredBlockResets));
			teleporterBlockEntity.setTeleporterName(teleporterName);
			teleporterBlockEntity.setCurrentTargetIdentifierLabel(currentTargetIdentifierLabel);
			teleporterBlockEntity.setCurrentTargetOwnerLabel(currentTargetOwnerLabel);
			teleporterBlockEntity.setShowCancelButton(showCancelButton);
			teleporterBlockEntity.setCanOwnerBeChosen(canOwnerBeChosen);
			teleporterBlockEntity.setTeleportButtonLabel(teleportButtonLabel);
			teleporterBlockEntity.setCancelTeleportButtonLabel(cancelTeleportButtonLabel);

			if (updateSuccessful) {
				serverPlayerEntity.displayClientMessage(Component.translatable("hud.message.script_block.update_successful"), true);
			}
			teleporterBlockEntity.setChanged();
			world.sendBlockUpdated(teleportBlockPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
