package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportToTeamPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TeleportToTeamPacket> {
	@Override
	public void receive(TeleportToTeamPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		Identifier targetWorldIdentifier = payload.targetWorldIdentifier();

		BlockPos targetPos = payload.targetPosition();

		double targetYaw = payload.targetYaw();
		double targetPitch = payload.targetPitch();

		MinecraftServer server = serverPlayerEntity.server;

		RegistryKey<World> dimensionregistryKey = RegistryKey.of(RegistryKeys.WORLD, targetWorldIdentifier);
		ServerWorld targetWorld = server.getWorld(dimensionregistryKey);

		if (targetWorld != null) {

			serverPlayerEntity.fallDistance = 0;
			serverPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.01), (targetPos.getZ() + 0.5), (float) targetYaw, (float) targetPitch);
			if (ScriptBlocks.serverConfig.show_debug_messages) {
				serverPlayerEntity.sendMessage(Text.of("Teleport to your team in world: " + targetWorld.getRegistryKey().getValue() + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.01) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + targetYaw + " and pitch: " + targetPitch));
			}
			serverPlayerEntity.closeHandledScreen();

		}
	}
}
