package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.component.type.InteractiveKeyComponent;
import com.github.theredbrain.scriptblocks.component.type.RemovedOnTeleportComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemComponentRegistry {
	public static final ComponentType<BlockPositionDistanceMeterComponent> BLOCK_POSITION_DISTANCE_METER = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("block_position_distance_meter"),
			ComponentType.<BlockPositionDistanceMeterComponent>builder().codec(BlockPositionDistanceMeterComponent.CODEC).build()
	);

	public static final ComponentType<InteractiveKeyComponent> INTERACTIVE_KEY = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("interactive_key"),
			ComponentType.<InteractiveKeyComponent>builder().codec(InteractiveKeyComponent.CODEC).packetCodec(InteractiveKeyComponent.PACKET_CODEC).cache().build()
	);

	public static final ComponentType<RemovedOnTeleportComponent> REMOVED_ON_TELEPORT = Registry.register(
			Registries.DATA_COMPONENT_TYPE,
			ScriptBlocks.identifier("removed_on_teleport"),
			ComponentType.<RemovedOnTeleportComponent>builder().codec(RemovedOnTeleportComponent.CODEC).packetCodec(RemovedOnTeleportComponent.PACKET_CODEC).cache().build()
	);

	public static void init() {
	}
}
