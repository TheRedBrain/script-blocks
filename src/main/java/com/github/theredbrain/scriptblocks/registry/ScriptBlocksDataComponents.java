package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.component.type.PreventInteractionsDataComponent;
import com.github.theredbrain.scriptblocks.component.type.RemovedOnInteractionsDataComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class ScriptBlocksDataComponents {
	public static final DataComponentType<BlockPositionDistanceMeterComponent> BLOCK_POSITION_DISTANCE_METER = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("block_position_distance_meter"),
			DataComponentType.<BlockPositionDistanceMeterComponent>builder().persistent(BlockPositionDistanceMeterComponent.CODEC).build()
	);

	public static final DataComponentType<InteractiveKeyComponent> INTERACTIVE_KEY = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("interactive_key"),
			DataComponentType.<InteractiveKeyComponent>builder().persistent(InteractiveKeyComponent.CODEC).networkSynchronized(InteractiveKeyComponent.PACKET_CODEC).cacheEncoding().build()
	);

	public static final DataComponentType<RemovedOnInteractionsDataComponent> REMOVED_ON_INTERACTIONS = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("removed_on_interactions"),
			DataComponentType.<RemovedOnInteractionsDataComponent>builder().persistent(RemovedOnInteractionsDataComponent.CODEC).networkSynchronized(RemovedOnInteractionsDataComponent.PACKET_CODEC).cacheEncoding().build()
	);

	public static final DataComponentType<PreventInteractionsDataComponent> PREVENT_INTERACTIONS = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("prevent_interactions"),
			DataComponentType.<PreventInteractionsDataComponent>builder().persistent(PreventInteractionsDataComponent.CODEC).networkSynchronized(PreventInteractionsDataComponent.PACKET_CODEC).cacheEncoding().build()
	);

	public static void bootstrap() {
	}
}
