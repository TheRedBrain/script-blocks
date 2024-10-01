package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.registry.ComponentsRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;

public class LeaveHouseFromHousingScreenPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<LeaveHouseFromHousingScreenPacket> {
	@Override
	public void receive(LeaveHouseFromHousingScreenPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		MinecraftServer server = serverPlayerEntity.server;
		ServerWorld targetWorld = null;
		BlockPos targetPos = null;
		Pair<Pair<String, BlockPos>, Boolean> housing_access_pos = ComponentsRegistry.PLAYER_LOCATION_ACCESS_POS.get(serverPlayerEntity).getValue();
		if (housing_access_pos.getRight()) {
			targetWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, Identifier.of(housing_access_pos.getLeft().getLeft())));
			targetPos = housing_access_pos.getLeft().getRight();
			if (targetWorld != null && targetPos != null) {
				ComponentsRegistry.PLAYER_LOCATION_ACCESS_POS.get(serverPlayerEntity).deactivate();
			}
		} else {
			targetWorld = server.getWorld(serverPlayerEntity.getSpawnPointDimension());
			targetPos = serverPlayerEntity.getSpawnPointPosition();
		}
		if (targetWorld != null && targetPos != null) {

			serverPlayerEntity.teleport(targetWorld, (targetPos.getX() + 0.5), (targetPos.getY() + 0.5), (targetPos.getZ() + 0.5), (float) 0.0, (float) 0.0);
			if (ScriptBlocks.serverConfig.show_debug_messages) {
				serverPlayerEntity.sendMessage(Text.of("Teleport to world: " + targetWorld + " at position: " + (targetPos.getX() + 0.5) + ", " + (targetPos.getY() + 0.5) + ", " + (targetPos.getZ() + 0.5) + ", with yaw: " + 0.0 + " and pitch: " + 0.0));
			}
		} else if (ScriptBlocks.serverConfig.show_debug_messages) {
			serverPlayerEntity.sendMessage(Text.of("Teleport failed"));
			if (targetWorld == null) {
				serverPlayerEntity.sendMessage(Text.of("targetWorld == null"));
			}
			if (targetPos == null) {
				serverPlayerEntity.sendMessage(Text.of("targetPos == null"));
			}
		}
	}
}
