package com.github.theredbrain.scriptblocks.world;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import qouteall.dimlib.DimensionTemplate;
import qouteall.dimlib.api.DimensionAPI;

public class DimensionsManager {

	public static void init() {
		DimensionAPI.suppressExperimentalWarningForNamespace(ScriptBlocks.MOD_ID);
		DimensionAPI.registerDimensionTemplate(
				"player_locations", PLAYER_LOCATIONS_DIMENSION_TEMPLATE
		);
	}

	public static void addAndSaveDynamicDimension(Identifier dimensionId, MinecraftServer server) {
		DimensionAPI.addDimensionDynamically(server, dimensionId, PLAYER_LOCATIONS_DIMENSION_TEMPLATE.createLevelStem(server));
	}

	public static final DimensionTemplate PLAYER_LOCATIONS_DIMENSION_TEMPLATE = new DimensionTemplate(
			DimensionTypes.OVERWORLD,
			(server, dimTypeHolder) -> {
				DynamicRegistryManager.Immutable registryAccess = server.getRegistryManager();

				Registry<FlatLevelGeneratorPreset> flatLevelGeneratorPresetRegistry = registryAccess.get(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET);

				RegistryEntry.Reference<FlatLevelGeneratorPreset> flatLevelGeneratorPresetReference = flatLevelGeneratorPresetRegistry.entryOf(RegistryKey.of(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, ScriptBlocks.identifier("player_locations_dimension")));

				FlatChunkGenerator chunkGenerator = new FlatChunkGenerator(flatLevelGeneratorPresetReference.value().settings());

				return new DimensionOptions(dimTypeHolder, chunkGenerator);
			}
	);
}
