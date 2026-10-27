package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.MutablePair;

public class LeaveHouseFromHousingScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<LeaveHouseFromHousingScreenPacket> {
	@Override
	public void receive(LeaveHouseFromHousingScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		MinecraftServer server = serverPlayerEntity.server;
		ServerLevel targetWorld = null;
		BlockPos targetPos = null;
		MutablePair<String, BlockPos> location_access_pos = DataAttachmentHelper.getCurrentLocationAccess(serverPlayerEntity);
		if (location_access_pos != null) {
			targetWorld = server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(location_access_pos.getLeft())));
			targetPos = location_access_pos.getRight();
			if (targetWorld != null && targetPos != null) {
				DataAttachmentHelper.setCurrentLocationAccess(serverPlayerEntity, null);
			}
		} else {
			targetWorld = server.getLevel(serverPlayerEntity.getSpawnPointDimension());
			targetPos = serverPlayerEntity.getSpawnPointPosition();
		}
		if (targetWorld != null && targetPos != null) {

			serverPlayerEntity.teleportTo(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.5), (targetPos.getZ() + 0.5), (float) 0.0, (float) 0.0);
			if (DebuggingHelper.isTeleporterLoggingEnabled()) {
				DebuggingHelper.sendDebuggingMessage("Teleport to world: " + targetWorld + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.5) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + 0.0 + " and pitch: " + 0.0, serverPlayerEntity);
			}
		} else if (DebuggingHelper.isTeleporterLoggingEnabled()) {
			DebuggingHelper.sendDebuggingMessage("Teleport failed", serverPlayerEntity);
			if (targetWorld == null) {
				DebuggingHelper.sendDebuggingMessage("targetWorld == null", serverPlayerEntity);
			}
			if (targetPos == null) {
				DebuggingHelper.sendDebuggingMessage("targetPos == null", serverPlayerEntity);
			}
		}
	}
}
