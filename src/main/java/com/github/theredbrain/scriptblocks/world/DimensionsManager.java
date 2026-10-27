package com.github.theredbrain.scriptblocks.world;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;

import java.util.Optional;

public class DimensionsManager {

	public static Identifier PUBLIC_LOCATIONS_DIMENSION_IDENTIFIER = ScriptBlocks.identifier("public_locations_dimension");

	public static void bootstrap() {
	}

	public static void addAndSavePublicDimension(Identifier dimensionId, MinecraftServer server) {

		Fantasy fantasy = Fantasy.get(server);

		RuntimeLevelConfig worldConfig = new RuntimeLevelConfig()
				.setDimensionType(BuiltinDimensionTypes.OVERWORLD)
				.setFlat(true)
				.setMirrorOverworldDifficulty(true)
				.setMirrorOverworldGameRules(true)
				.setGenerator(server.overworld().getChunkSource().getGenerator());

		fantasy.getOrOpenPersistentLevel(dimensionId, worldConfig);

	}

	public static void addAndSaveDynamicDimension(Identifier dimensionId, MinecraftServer server) {

		Fantasy fantasy = Fantasy.get(server);

		RegistryAccess.Frozen registryAccess = server.registryAccess();

		Optional<Holder.Reference<Registry<FlatLevelGeneratorPreset>>> optionalFlatLevelGeneratorPresetRegistryReference = registryAccess.get(Registries.FLAT_LEVEL_GENERATOR_PRESET);

		if (optionalFlatLevelGeneratorPresetRegistryReference.isPresent()) {

			Optional<Holder.Reference<FlatLevelGeneratorPreset>> optionalFlatLevelGeneratorPresetReference = optionalFlatLevelGeneratorPresetRegistryReference.get().value().get(ResourceKey.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, ScriptBlocks.identifier("player_locations_dimension")));

			if (optionalFlatLevelGeneratorPresetReference.isPresent()) {

				FlatLevelSource chunkGenerator = new FlatLevelSource(optionalFlatLevelGeneratorPresetReference.get().value().settings());

				RuntimeLevelConfig worldConfig = new RuntimeLevelConfig()
						.setDimensionType(BuiltinDimensionTypes.OVERWORLD)
						.setFlat(true)
						.setMirrorOverworldDifficulty(true)
						.setMirrorOverworldGameRules(true)
						.setGenerator(chunkGenerator);

				fantasy.getOrOpenPersistentLevel(dimensionId, worldConfig);
			}
		}
	}
}
