package com.github.theredbrain.scriptblocks.mixin.server.network;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.portal.TeleportTransition;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements DuckPlayerEntityMixin {

	public ServerPlayerEntityMixin(Level world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@WrapMethod(method = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;")
	private static TeleportTransition scriptblocks$wrap_findRespawnPositionAndUseSpawnBlock() {

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
			MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint = triggeringRespawnAnchorBlockEntity.getActualSpawnPoint(world);
			BlockState blockState = world.getBlockState(spawnPoint.getLeft());
			Block block = blockState.getBlock();
			boolean bl = block.isPossibleToRespawnInThis(blockState);
			BlockState blockState2 = world.getBlockState(pos.above());
			boolean bl2 = blockState2.getBlock().isPossibleToRespawnInThis(blockState2);
			if (bl && bl2) {
				triggeringRespawnAnchorBlockEntity.trigger();
				return Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3((double) spawnPoint.getLeft().getX() + 0.5, (double) spawnPoint.getLeft().getY() + 0.1, (double) spawnPoint.getLeft().getZ() + 0.5), (float) spawnPoint.getRight().getLeft().doubleValue()));
			}
		}
		return original.call(world, pos, spawnAngle, spawnForced, alive);
	}
}
