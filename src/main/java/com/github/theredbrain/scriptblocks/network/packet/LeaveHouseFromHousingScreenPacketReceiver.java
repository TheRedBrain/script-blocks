package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

public class LeaveHouseFromHousingScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<LeaveHouseFromHousingScreenPacket> {
	@Override
	public void receive(LeaveHouseFromHousingScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		MinecraftServer server = serverPlayerEntity.server;
		ServerWorld targetWorld = null;
		BlockPos targetPos = null;
		MutablePair<String, BlockPos> location_access_pos = ((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$getLocationAccessPosition();
		if (location_access_pos != null) {
			targetWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(location_access_pos.getLeft())));
			targetPos = location_access_pos.getRight();
			if (targetWorld != null && targetPos != null) {
				((DuckPlayerEntityMixin) serverPlayerEntity).scriptblocks$setLocationAccessPosition(null);
			}
		} else {
			targetWorld = server.getWorld(serverPlayerEntity.getSpawnPointDimension());
			targetPos = serverPlayerEntity.getSpawnPointPosition();
		}
		if (targetWorld != null && targetPos != null) {

			serverPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.5), (targetPos.getZ() + 0.5), (float) 0.0, (float) 0.0);
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
