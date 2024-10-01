package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.world.gen.chunk.placement.FixedStructurePlacement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;

public class StructurePlacementTypesRegistry {
	public static StructurePlacementType<FixedStructurePlacement> FIXED = () -> {
		return FixedStructurePlacement.CODEC;
	};

	public static void register() {
		Registry.register(Registries.STRUCTURE_PLACEMENT, ScriptBlocks.identifier("fixed"), FIXED);
	}
}
