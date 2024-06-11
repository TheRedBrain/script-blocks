package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.*;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;

public class BlockRegistry {

    //region Content Blocks
    // content script blocks
    public static final Block USE_RELAY_OAK_DOOR = registerBlock("use_relay_oak_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_IRON_DOOR = registerBlock("use_relay_iron_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_SPRUCE_DOOR = registerBlock("use_relay_spruce_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_BIRCH_DOOR = registerBlock("use_relay_birch_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_JUNGLE_DOOR = registerBlock("use_relay_jungle_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_ACACIA_DOOR = registerBlock("use_relay_acacia_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_CHERRY_DOOR = registerBlock("use_relay_cherry_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_DARK_OAK_DOOR = registerBlock("use_relay_dark_oak_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_MANGROVE_DOOR = registerBlock("use_relay_mangrove_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_BAMBOO_DOOR = registerBlock("use_relay_bamboo_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_CRIMSON_DOOR = registerBlock("use_relay_crimson_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_WARPED_DOOR = registerBlock("use_relay_warped_door", new UseRelayDoorBlock(FabricBlockSettings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_OAK_TRAPDOOR = registerBlock("use_relay_oak_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_IRON_TRAPDOOR = registerBlock("use_relay_iron_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_SPRUCE_TRAPDOOR = registerBlock("use_relay_spruce_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_BIRCH_TRAPDOOR = registerBlock("use_relay_birch_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_JUNGLE_TRAPDOOR = registerBlock("use_relay_jungle_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_ACACIA_TRAPDOOR = registerBlock("use_relay_acacia_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_CHERRY_TRAPDOOR = registerBlock("use_relay_cherry_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_DARK_OAK_TRAPDOOR = registerBlock("use_relay_dark_oak_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_MANGROVE_TRAPDOOR = registerBlock("use_relay_mangrove_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_BAMBOO_TRAPDOOR = registerBlock("use_relay_bamboo_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_CRIMSON_TRAPDOOR = registerBlock("use_relay_crimson_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_WARPED_TRAPDOOR = registerBlock("use_relay_warped_trapdoor", new UseRelayTrapdoorBlock(FabricBlockSettings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_CHEST = registerBlock("use_relay_chest", new UseRelayChestBlock(null, FabricBlockSettings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block LOCKED_USE_RELAY_CHEST = registerBlock("locked_use_relay_chest", new UseRelayChestBlock(Tags.KEYS_FOR_LOCKED_USE_RELAY_CHEST, FabricBlockSettings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    //endregion Content Blocks

    //region Script Blocks
    public static final Block AREA_BLOCK = registerBlock("area_block", new AreaBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block BOSS_CONTROLLER_BLOCK = registerBlock("boss_controller_block", new BossControllerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block DELAY_TRIGGER_BLOCK = registerBlock("delay_trigger_block", new DelayTriggerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block DIALOGUE_BLOCK = registerBlock("dialogue_block", new DialogueBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block ENTRANCE_DELEGATION_BLOCK = registerBlock("entrance_delegation_block", new EntranceDelegationBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block HOUSING_BLOCK = registerBlock("housing_block", new HousingBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block INTERACTIVE_LOOT_BLOCK = registerBlock("interactive_loot_block", new InteractiveLootBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block JIGSAW_PLACER_BLOCK = registerBlock("jigsaw_placer_block", new JigsawPlacerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block LOCATION_CONTROL_BLOCK = registerBlock("location_control_block", new LocationControlBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block MIMIC_BLOCK = registerBlock("mimic_block", new MimicBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block MIMIC_FALLBACK_BLOCK = Registry.register(Registries.BLOCK, ScriptBlocksMod.identifier("mimic_fallback_block"), new Block(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()));
    public static final Block REDSTONE_TRIGGER_BLOCK = registerBlock("redstone_trigger_block", new RedstoneTriggerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block RELAY_TRIGGER_BLOCK = registerBlock("relay_trigger_block", new RelayTriggerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block SHOP_BLOCK = registerBlock("shop_block", new ShopBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block TELEPORTER_BLOCK = registerBlock("teleporter_block", new TeleporterBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block TRIGGERED_ADVANCEMENT_CHECKER_BLOCK = registerBlock("triggered_advancement_checker_block", new TriggeredAdvancementCheckerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block TRIGGERED_COUNTER_BLOCK = registerBlock("triggered_counter_block", new TriggeredCounterBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block TRIGGERED_SPAWNER_BLOCK = registerBlock("triggered_spawner_block", new TriggeredSpawnerBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
    public static final Block USE_RELAY_BLOCK = registerBlock("use_relay_block", new UseRelayBlock(FabricBlockSettings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
    //endregion Script Blocks

    private static Block registerBlock(String name, Block block, RegistryKey<ItemGroup> itemGroup) {
        Registry.register(Registries.ITEM, ScriptBlocksMod.identifier(name), new BlockItem(block, new FabricItemSettings()));
        ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(block));
        return Registry.register(Registries.BLOCK, ScriptBlocksMod.identifier(name), block);
    }

    public static void init() {}
}
