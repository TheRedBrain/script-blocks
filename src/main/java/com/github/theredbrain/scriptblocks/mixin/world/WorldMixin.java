package com.github.theredbrain.scriptblocks.mixin.world;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess {
	@Shadow
	@Final
	protected MutableWorldProperties properties;

	@Shadow
	@Final
	public Random random;

	@WrapMethod(method = "getSpawnPos")
	public BlockPos scriptblocks$wrap_getSpawnPos(Operation<BlockPos> original) {

		BlockPos blockPos = null;
		ServerConfig serverConfig = ScriptBlocks.SERVER_CONFIG;
		if (serverConfig.use_predefined_position_for_world_spawn) {
			List<Integer> worldSpawnXList = ScriptBlocks.SERVER_CONFIG.worldSpawnXList;
			List<Integer> worldSpawnYList = ScriptBlocks.SERVER_CONFIG.worldSpawnYList;
			List<Integer> worldSpawnZList = ScriptBlocks.SERVER_CONFIG.worldSpawnZList;
			int listSize = worldSpawnXList.size();
			if (listSize > 0) {
				int spawnPointIndex = this.random.nextBetweenExclusive(0, listSize);
				if (spawnPointIndex < worldSpawnXList.size() && spawnPointIndex < worldSpawnYList.size() && spawnPointIndex < worldSpawnZList.size()) {
					blockPos = new BlockPos(worldSpawnXList.get(spawnPointIndex), worldSpawnYList.get(spawnPointIndex), worldSpawnZList.get(spawnPointIndex));
				}
			}
		}
		World world = (World) (Object) this;
		if (serverConfig.use_location_entrance_for_world_spawn && !serverConfig.world_spawn_location_identifier.isEmpty() && world instanceof ServerWorld serverWorld) {

			Location location = null;
			Optional<RegistryEntry.Reference<Location>> optionalLocationReference = this.getRegistryManager().get(CustomDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(serverConfig.world_spawn_location_identifier));

			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}

			if (location != null) {
				BlockEntity blockEntity = this.getBlockEntity(LocationUtils.getControlBlockPosForLocation(location));

				if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {

					MutablePair<BlockPos, MutablePair<Double, Double>> entrance = locationControlBlock.getTargetEntrance(serverWorld, serverConfig.world_spawn_entrance_identifier);
					blockPos = entrance.getLeft();
				}
			}
		}
		if (blockPos != null && this.getWorldBorder().contains(blockPos)) {
			return blockPos;
		}
		return original.call();
	}
}
