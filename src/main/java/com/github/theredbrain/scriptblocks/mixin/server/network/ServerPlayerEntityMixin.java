package com.github.theredbrain.scriptblocks.mixin.server.network;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringRespawnAnchorBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements DuckPlayerEntityMixin {

	public ServerPlayerEntityMixin(Level level, GameProfile gameProfile) {
		super(level, gameProfile);
	}

	@WrapMethod(method = "findRespawnAndUseSpawnBlock(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)Ljava/util/Optional;")
	private static Optional<ServerPlayer.RespawnPosAngle> scriptblocks$wrap_findRespawnAndUseSpawnBlock(ServerLevel level, ServerPlayer.RespawnConfig respawnConfig, boolean consumeSpawnBlock, Operation<Optional<ServerPlayer.RespawnPosAngle>> original) {

		BlockEntity blockEntity = level.getBlockEntity(respawnConfig.respawnData().pos());
		if (blockEntity instanceof TriggeringRespawnAnchorBlockEntity triggeringRespawnAnchorBlockEntity) {
			LevelData.RespawnData spawnPoint = triggeringRespawnAnchorBlockEntity.getActualSpawnPoint(level);
			BlockState blockState = level.getBlockState(spawnPoint.pos());
			Block block = blockState.getBlock();
			boolean bl = block.isPossibleToRespawnInThis(blockState);
			BlockState blockState2 = level.getBlockState(spawnPoint.pos().above());
			boolean bl2 = blockState2.getBlock().isPossibleToRespawnInThis(blockState2);
			if (bl && bl2) {
				triggeringRespawnAnchorBlockEntity.trigger();
				return Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3(spawnPoint.pos().getX() + 0.5, spawnPoint.pos().getY() + 0.1, spawnPoint.pos().getZ() + 0.5), spawnPoint.yaw(), spawnPoint.pitch()));
			}
		}
		return original.call(level, respawnConfig, consumeSpawnBlock);
	}
}
