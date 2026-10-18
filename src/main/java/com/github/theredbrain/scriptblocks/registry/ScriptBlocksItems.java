package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.component.type.BlockPositionDistanceMeterComponent;
import com.github.theredbrain.scriptblocks.item.BlockPositionDistanceMeterItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Direction;

import java.util.List;

public class ScriptBlocksItems {

	public static final Item BLOCK_POSITION_DISTANCE_METER = registerItem("block_position_distance_meter", new BlockPositionDistanceMeterItem(new Item.Settings().maxCount(1).component(ScriptBlocksDataComponents.BLOCK_POSITION_DISTANCE_METER, BlockPositionDistanceMeterComponent.DEFAULT)), List.of(ItemGroupRegistry.SCRIPT_BLOCKS));
	public static final Item INTERACTIVE_TRIGGER_TORCH = registerItem("interactive_trigger_torch", new VerticallyAttachableBlockItem(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_TORCH, ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WALL_TORCH, new Item.Settings(), Direction.DOWN), List.of(ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS));
	public static final Item INTERACTIVE_TRIGGER_SOUL_TORCH = registerItem("interactive_trigger_soul_torch", new VerticallyAttachableBlockItem(ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SOUL_TORCH, ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SOUL_WALL_TORCH, new Item.Settings(), Direction.DOWN), List.of(ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS));

	private static Item registerItem(String name, Item item, List<RegistryKey<ItemGroup>> itemGroupList) {

		for (RegistryKey<ItemGroup> itemGroup : itemGroupList) {
			ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(item));
		}
		return Registry.register(Registries.ITEM, ScriptBlocks.identifier(name), item);
	}

	public static void bootstrap() {
	}
}
