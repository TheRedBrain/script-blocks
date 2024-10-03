package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemComponentRegistry {
	public static final ComponentType<BlockPositionDistanceMeterComponent> BLOCK_POSITION_DISTANCE_METER = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("block_position_distance_meter"),
			ComponentType.<BlockPositionDistanceMeterComponent>builder().codec(BlockPositionDistanceMeterComponent.CODEC).build()
	);

	public static void init() {
	}
}
