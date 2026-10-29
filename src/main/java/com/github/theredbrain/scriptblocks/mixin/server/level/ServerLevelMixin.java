package com.github.theredbrain.scriptblocks.mixin.server.level;

import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.util.LocationUtils;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Optional;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

	protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
		super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
	}

	@WrapMethod(method = "getRespawnData")
	public LevelData.RespawnData scriptblocks$wrap_getRespawnData(Operation<LevelData.RespawnData> original) {

		LevelData.RespawnData spawnPoint = null;
		ServerConfig serverConfig = ScriptBlocksConfigs.SERVER_CONFIG;
		if (serverConfig.use_predefined_position_for_world_spawn) {
			List<Integer> worldSpawnXList = ScriptBlocksConfigs.SERVER_CONFIG.worldSpawnXList;
			List<Integer> worldSpawnYList = ScriptBlocksConfigs.SERVER_CONFIG.worldSpawnYList;
			List<Integer> worldSpawnZList = ScriptBlocksConfigs.SERVER_CONFIG.worldSpawnZList;
			int listSize = worldSpawnXList.size();
			if (listSize > 0) {
				int spawnPointIndex = this.random.nextInt(0, listSize);
				if (spawnPointIndex < worldSpawnXList.size() && spawnPointIndex < worldSpawnYList.size() && spawnPointIndex < worldSpawnZList.size()) {
					spawnPoint = LevelData.RespawnData.of(this.dimension(), new BlockPos(worldSpawnXList.get(spawnPointIndex), worldSpawnYList.get(spawnPointIndex), worldSpawnZList.get(spawnPointIndex)), 0.0F, 0.0F);
				}
			}
		}
		if (serverConfig.use_location_entrance_for_world_spawn && !serverConfig.world_spawn_location_identifier.isEmpty()) {

			Identifier identifier = Identifier.tryParse(serverConfig.world_spawn_location_identifier);
			if (identifier == null) {
				return original.call();
			}
			Optional<Holder.Reference<Location>> optionalLocationReference = this.registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).get().value().get(identifier);

			Location location = null;
			if (optionalLocationReference.isPresent()) {
				location = optionalLocationReference.get().value();
			}

			if (location != null) {
				BlockEntity blockEntity = this.getBlockEntity(LocationUtils.getControlBlockPosForLocation(location));

				if (blockEntity instanceof LocationControlBlockEntity locationControlBlock) {

					spawnPoint = locationControlBlock.getActualSpawnPoint(this, serverConfig.world_spawn_entrance_identifier);
				}
			}
		}

		if (spawnPoint != null && this.getWorldBorder().isWithinBounds(spawnPoint.pos())) {
			return spawnPoint;
		}
		return original.call();
	}
}
