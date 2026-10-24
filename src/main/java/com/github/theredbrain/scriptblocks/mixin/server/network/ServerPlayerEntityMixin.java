package com.github.theredbrain.scriptblocks.mixin.server.network;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements DuckPlayerEntityMixin {

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@WrapMethod(method = "findRespawnPosition(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;FZZ)Ljava/util/Optional;")
	private static Optional<ServerPlayerEntity.RespawnPos> scriptblocks$wrap_findRespawnPosition(ServerWorld world, BlockPos pos, float spawnAngle, boolean spawnForced, boolean alive, Operation<Optional<ServerPlayerEntity.RespawnPos>> original) {

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
			MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint = triggeringRespawnAnchorBlockEntity.getActualSpawnPoint(world);
			BlockState blockState = world.getBlockState(spawnPoint.getLeft());
			Block block = blockState.getBlock();
			boolean bl = block.canMobSpawnInside(blockState);
			BlockState blockState2 = world.getBlockState(pos.up());
			boolean bl2 = blockState2.getBlock().canMobSpawnInside(blockState2);
			if (bl && bl2) {
				triggeringRespawnAnchorBlockEntity.trigger();
				return Optional.of(new ServerPlayerEntity.RespawnPos(new Vec3d((double) spawnPoint.getLeft().getX() + 0.5, (double) spawnPoint.getLeft().getY() + 0.1, (double) spawnPoint.getLeft().getZ() + 0.5), (float) spawnPoint.getRight().getLeft().doubleValue()));
			}
		}
		return original.call(world, pos, spawnAngle, spawnForced, alive);
	}
}
