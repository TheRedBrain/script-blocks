package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ItemGroupRegistry {
	public static final RegistryKey<ItemGroup> SCRIPT_BLOCKS = RegistryKey.of(RegistryKeys.ITEM_GROUP, ScriptBlocksMod.identifier("script_blocks"));

	public static void init() {
		Registry.register(Registries.ITEM_GROUP, SCRIPT_BLOCKS, FabricItemGroup.builder()
				.icon(() -> new ItemStack(BlockRegistry.TELEPORTER_BLOCK))
				.displayName(Text.translatable("itemGroup.scriptblocks.script_blocks"))
				.build());
	}
}
