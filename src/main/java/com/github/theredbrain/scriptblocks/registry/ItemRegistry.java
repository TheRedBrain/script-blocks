package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.item.BlockPositionDistanceMeterItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public class ItemRegistry {

    public static final Item BLOCK_POSITION_DISTANCE_METER = registerItem("block_position_distance_meter", new BlockPositionDistanceMeterItem(new FabricItemSettings().maxCount(1)), ItemGroupRegistry.SCRIPT_BLOCKS);

    private static Item registerItem(String name, Item item, @Nullable RegistryKey<ItemGroup> itemGroup) {

        if (itemGroup != null) {
            ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> {
                content.add(item);
            });
        }
        return Registry.register(Registries.ITEM, ScriptBlocksMod.identifier(name), item);
    }

    public static void init() {
    }
}
