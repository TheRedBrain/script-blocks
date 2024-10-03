package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.AreaBlock;
import com.github.theredbrain.scriptblocks.block.BossControllerBlock;
//import com.github.theredbrain.scriptblocks.block.DataAccessBlock;
//import com.github.theredbrain.scriptblocks.block.DataSavingBlock;
import com.github.theredbrain.scriptblocks.block.DelayTriggerBlock;
import com.github.theredbrain.scriptblocks.block.DialogueBlock;
import com.github.theredbrain.scriptblocks.block.EntranceDelegationBlock;
import com.github.theredbrain.scriptblocks.block.HousingBlock;
import com.github.theredbrain.scriptblocks.block.InteractiveLootBlock;
import com.github.theredbrain.scriptblocks.block.JigsawPlacerBlock;
import com.github.theredbrain.scriptblocks.block.LocationControlBlock;
import com.github.theredbrain.scriptblocks.block.MimicBlock;
import com.github.theredbrain.scriptblocks.block.RedstoneTriggerBlock;
import com.github.theredbrain.scriptblocks.block.RelayTriggerBlock;
import com.github.theredbrain.scriptblocks.block.ShopBlock;
import com.github.theredbrain.scriptblocks.block.TeleporterBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredAdvancementCheckerBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredCounterBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredSpawnerBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayChestBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayDoorBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayTrapdoorBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class BlockRegistry {

	//region Content Blocks
	// content script blocks
	public static final Block USE_RELAY_OAK_DOOR = registerBlock("use_relay_oak_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_IRON_DOOR = registerBlock("use_relay_iron_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_SPRUCE_DOOR = registerBlock("use_relay_spruce_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BIRCH_DOOR = registerBlock("use_relay_birch_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_JUNGLE_DOOR = registerBlock("use_relay_jungle_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_ACACIA_DOOR = registerBlock("use_relay_acacia_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CHERRY_DOOR = registerBlock("use_relay_cherry_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_DARK_OAK_DOOR = registerBlock("use_relay_dark_oak_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_MANGROVE_DOOR = registerBlock("use_relay_mangrove_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BAMBOO_DOOR = registerBlock("use_relay_bamboo_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CRIMSON_DOOR = registerBlock("use_relay_crimson_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_WARPED_DOOR = registerBlock("use_relay_warped_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_OAK_TRAPDOOR = registerBlock("use_relay_oak_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_IRON_TRAPDOOR = registerBlock("use_relay_iron_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_SPRUCE_TRAPDOOR = registerBlock("use_relay_spruce_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BIRCH_TRAPDOOR = registerBlock("use_relay_birch_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_JUNGLE_TRAPDOOR = registerBlock("use_relay_jungle_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_ACACIA_TRAPDOOR = registerBlock("use_relay_acacia_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CHERRY_TRAPDOOR = registerBlock("use_relay_cherry_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_DARK_OAK_TRAPDOOR = registerBlock("use_relay_dark_oak_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_MANGROVE_TRAPDOOR = registerBlock("use_relay_mangrove_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BAMBOO_TRAPDOOR = registerBlock("use_relay_bamboo_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CRIMSON_TRAPDOOR = registerBlock("use_relay_crimson_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_WARPED_TRAPDOOR = registerBlock("use_relay_warped_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CHEST = registerBlock("use_relay_chest", new UseRelayChestBlock(null, Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block LOCKED_USE_RELAY_CHEST = registerBlock("locked_use_relay_chest", new UseRelayChestBlock(Tags.KEYS_FOR_LOCKED_USE_RELAY_CHEST, Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	//endregion Content Blocks

	//region Script Blocks
	public static final Block AREA_BLOCK = registerBlock("area_block", new AreaBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block BOSS_CONTROLLER_BLOCK = registerBlock("boss_controller_block", new BossControllerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
//	public static final Block DATA_ACCESS_BLOCK = registerBlock("data_access_block", new DataAccessBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
//	public static final Block DATA_SAVING_BLOCK = registerBlock("data_saving_block", new DataSavingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DELAY_TRIGGER_BLOCK = registerBlock("delay_trigger_block", new DelayTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DIALOGUE_BLOCK = registerBlock("dialogue_block", new DialogueBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block ENTRANCE_DELEGATION_BLOCK = registerBlock("entrance_delegation_block", new EntranceDelegationBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block HOUSING_BLOCK = registerBlock("housing_block", new HousingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_LOOT_BLOCK = registerBlock("interactive_loot_block", new InteractiveLootBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block JIGSAW_PLACER_BLOCK = registerBlock("jigsaw_placer_block", new JigsawPlacerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block LOCATION_CONTROL_BLOCK = registerBlock("location_control_block", new LocationControlBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block MIMIC_BLOCK = registerBlock("mimic_block", new MimicBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block MIMIC_FALLBACK_BLOCK = Registry.register(Registries.BLOCK, ScriptBlocks.identifier("mimic_fallback_block"), new Block(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()));
	public static final Block REDSTONE_TRIGGER_BLOCK = registerBlock("redstone_trigger_block", new RedstoneTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block RELAY_TRIGGER_BLOCK = registerBlock("relay_trigger_block", new RelayTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block SHOP_BLOCK = registerBlock("shop_block", new ShopBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BLOCK = registerBlock("teleporter_block", new TeleporterBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_ADVANCEMENT_CHECKER_BLOCK = registerBlock("triggered_advancement_checker_block", new TriggeredAdvancementCheckerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_COUNTER_BLOCK = registerBlock("triggered_counter_block", new TriggeredCounterBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_SPAWNER_BLOCK = registerBlock("triggered_spawner_block", new TriggeredSpawnerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BLOCK = registerBlock("use_relay_block", new UseRelayBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	//endregion Script Blocks

	private static Block registerBlock(String name, Block block, RegistryKey<ItemGroup> itemGroup) {
		Registry.register(Registries.ITEM, ScriptBlocks.identifier(name), new BlockItem(block, new Item.Settings()));
		ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(block));
		return Registry.register(Registries.BLOCK, ScriptBlocks.identifier(name), block);
	}

	public static void init() {
	}
}
