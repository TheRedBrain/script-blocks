package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Set;

public class TeleportToTeamPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TeleportToTeamPacket> {
	@Override
	public void receive(TeleportToTeamPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		Identifier targetWorldIdentifier = payload.targetWorldIdentifier();

		BlockPos targetPos = payload.targetPosition();

		double targetYaw = payload.targetYaw();
		double targetPitch = payload.targetPitch();

		ServerLevel serverLevel = serverPlayerEntity.level();
		MinecraftServer server = serverLevel.getServer();

		ResourceKey<Level> dimensionregistryKey = ResourceKey.create(Registries.DIMENSION, targetWorldIdentifier);
		ServerLevel targetWorld = server.getLevel(dimensionregistryKey);

		if (targetWorld != null) {

			serverPlayerEntity.fallDistance = 0;
			serverPlayerEntity.teleportTo(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), Set.of(), (float) targetYaw, (float) targetPitch, true);
			if (DebuggingHelper.isTeleporterLoggingEnabled()) {
				DebuggingHelper.sendDebuggingMessage("Teleport to your team in world: " + targetWorld.dimension().identifier() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch, serverPlayerEntity);
			}
			serverPlayerEntity.closeContainer();

		}
	}
}
