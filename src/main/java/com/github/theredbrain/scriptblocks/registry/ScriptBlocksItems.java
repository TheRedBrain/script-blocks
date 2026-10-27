package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.item.BlockPositionDistanceMeterItem;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.List;

public class ScriptBlocksItems {

	public static ResourceKey<Item> BLOCK_POSITION_DISTANCE_METER_KEY = ResourceKey.create(Registries.ITEM, ScriptBlocks.identifier("block_position_distance_meter"));
	public static final Item BLOCK_POSITION_DISTANCE_METER = registerItem(BLOCK_POSITION_DISTANCE_METER_KEY, new BlockPositionDistanceMeterItem(new Item.Properties().setId(BLOCK_POSITION_DISTANCE_METER_KEY).stacksTo(1).component(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER, BlockPositionDistanceMeterComponent.DEFAULT)), List.of(ItemGroupRegistry.SCRIPT_BLOCKS));

	private static Item registerItem(ResourceKey<Item> key, Item item, List<ResourceKey<CreativeModeTab>> creativeModeTabList) {

		for (ResourceKey<CreativeModeTab> creativeModeTab : creativeModeTabList) {
			CreativeModeTabEvents.modifyOutputEvent(creativeModeTab).register(content -> {
				content.accept(item);
			});
		}

		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void bootstrap() {
	}
}
