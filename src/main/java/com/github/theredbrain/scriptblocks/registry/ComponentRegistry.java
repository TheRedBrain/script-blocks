package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ComponentRegistry {
	public static final ComponentType<BlockPositionDistanceMeterComponent> BLOCK_POSITION_DISTANCE_METER = register(
			"block_position_distance_meter", builder -> builder.codec(BlockPositionDistanceMeterComponent.CODEC).packetCodec(BlockPositionDistanceMeterComponent.PACKET_CODEC).cache()
	);

	private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
		return Registry.register(Registries.DATA_COMPONENT_TYPE, id, ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build());
	}
	public static void init() {
	}
}
