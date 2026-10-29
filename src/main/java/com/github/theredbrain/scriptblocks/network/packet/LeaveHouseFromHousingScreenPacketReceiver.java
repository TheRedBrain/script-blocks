package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelData;

import java.util.Set;

public class LeaveHouseFromHousingScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<LeaveHouseFromHousingScreenPacket> {
	@Override
	public void receive(LeaveHouseFromHousingScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		MinecraftServer server = serverPlayerEntity.level().getServer();
		ServerLevel targetWorld = null;
		BlockPos targetPos = null;
		LevelData.RespawnData location_access_point = DataAttachmentHelper.getCurrentLocationAccessPoint(serverPlayerEntity);
		if (location_access_point != null) {
			targetPos = location_access_point.pos();
			targetWorld = server.getLevel(location_access_point.dimension());
			if (targetWorld != null) {
				DataAttachmentHelper.setCurrentLocationAccessPoint(serverPlayerEntity, null);
			}
		}
		if (targetPos != null && targetWorld != null) {

			serverPlayerEntity.teleportTo(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.5), (targetPos.getZ() + 0.5), Set.of(), (float) 0.0, (float) 0.0, true);
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
