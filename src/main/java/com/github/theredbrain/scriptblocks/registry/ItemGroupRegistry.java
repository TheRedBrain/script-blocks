package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroupRegistry {
	public static final ResourceKey<CreativeModeTab> DECORATIVE_SCRIPT_BLOCKS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ScriptBlocks.identifier("decorative_script_blocks"));
	public static final ResourceKey<CreativeModeTab> SCRIPT_BLOCKS = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ScriptBlocks.identifier("script_blocks"));

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DECORATIVE_SCRIPT_BLOCKS, FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(ScriptBlocksBlocks.AESTHETIC_NETHER_PORTAL))
				.title(Component.translatable("itemGroup.scriptblocks.decorative_script_blocks"))
				.build());
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, SCRIPT_BLOCKS, FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(ScriptBlocksBlocks.TELEPORTER_BLOCK))
				.title(Component.translatable("itemGroup.scriptblocks.script_blocks"))
				.build());
	}
}
