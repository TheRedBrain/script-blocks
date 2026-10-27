package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.world.gen.chunk.placement.FixedStructurePlacement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ScriptBlocksStructurePlacementTypes {
	public static StructurePlacementType<FixedStructurePlacement> FIXED = () -> {
		return FixedStructurePlacement.CODEC;
	};

	public static void init() {
		Registry.register(BuiltInRegistries.STRUCTURE_PLACEMENT, ScriptBlocks.identifier("fixed"), FIXED);
	}
}
