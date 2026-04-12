package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.AestheticDecoratedPotBlock;
import com.github.theredbrain.scriptblocks.block.AestheticVerticalPortalBlock;
import com.github.theredbrain.scriptblocks.block.AreaBlock;
import com.github.theredbrain.scriptblocks.block.AreaFillerBlock;
import com.github.theredbrain.scriptblocks.block.BossControllerBlock;
import com.github.theredbrain.scriptblocks.block.CopyDataBlock;
import com.github.theredbrain.scriptblocks.block.DataRelayBlock;
import com.github.theredbrain.scriptblocks.block.DataSavingBlock;
import com.github.theredbrain.scriptblocks.block.DataWritingBlock;
import com.github.theredbrain.scriptblocks.block.DelayTriggerBlock;
import com.github.theredbrain.scriptblocks.block.DialogueBlock;
import com.github.theredbrain.scriptblocks.block.EntranceDelegationBlock;
import com.github.theredbrain.scriptblocks.block.HousingBlock;
import com.github.theredbrain.scriptblocks.block.InteractiveLootBlock;
import com.github.theredbrain.scriptblocks.block.InteractiveTriggerCandleBlock;
import com.github.theredbrain.scriptblocks.block.InteractiveTriggerDoorBlock;
import com.github.theredbrain.scriptblocks.block.InteractiveTriggerTrapdoorBlock;
import com.github.theredbrain.scriptblocks.block.JigsawPlacerBlock;
import com.github.theredbrain.scriptblocks.block.LocationControlBlock;
import com.github.theredbrain.scriptblocks.block.LootableVaultBlock;
import com.github.theredbrain.scriptblocks.block.MimicBlock;
import com.github.theredbrain.scriptblocks.block.PVPControllerBlock;
import com.github.theredbrain.scriptblocks.block.PlayerDetectorBlock;
import com.github.theredbrain.scriptblocks.block.RedstoneTriggerBlock;
import com.github.theredbrain.scriptblocks.block.RelayTriggerBlock;
import com.github.theredbrain.scriptblocks.block.ShopBlock;
import com.github.theredbrain.scriptblocks.block.SpawnPointDelegationBlock;
import com.github.theredbrain.scriptblocks.block.TeamControllerBlock;
import com.github.theredbrain.scriptblocks.block.TeleporterBlock;
import com.github.theredbrain.scriptblocks.block.TeleporterDoorBlock;
import com.github.theredbrain.scriptblocks.block.TeleporterTrapdoorBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredAdvancementCheckerBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredBeaconBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredCounterBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredDamageDealingBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredDispenserBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredDisplayBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredEntityRemoverBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredRNGBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredRedstoneBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredSpawnerBlock;
import com.github.theredbrain.scriptblocks.block.TriggeredVillagerSpawnerBlock;
import com.github.theredbrain.scriptblocks.block.TriggeringTrialSpawnerBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayAnvilBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayChestBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayDoorBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayLecternBlock;
import com.github.theredbrain.scriptblocks.block.UseRelayTrapdoorBlock;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultState;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.Sherds;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;

public class BlockRegistry {

	//region Content Blocks
	public static final Block AESTHETIC_DECORATED_POT = registerAestheticDecoratedPotBlock("aesthetic_decorated_pot", new AestheticDecoratedPotBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_RED).strength(10.0F, 3600000.0f).sounds(BlockSoundGroup.DECORATED_POT).pistonBehavior(PistonBehavior.DESTROY).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block AESTHETIC_NETHER_PORTAL = registerBlock("aesthetic_nether_portal", new AestheticVerticalPortalBlock(Block.Settings.create().noCollision().strength(-1.0F).sounds(BlockSoundGroup.GLASS).luminance(state -> 11).pistonBehavior(PistonBehavior.BLOCK).dropsNothing(), ParticleTypes.PORTAL, SoundEvents.BLOCK_PORTAL_AMBIENT), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	
	public static final Block TELEPORTER_OAK_DOOR = registerBlock("teleporter_oak_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_IRON_DOOR = registerBlock("teleporter_iron_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_SPRUCE_DOOR = registerBlock("teleporter_spruce_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BIRCH_DOOR = registerBlock("teleporter_birch_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_JUNGLE_DOOR = registerBlock("teleporter_jungle_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_ACACIA_DOOR = registerBlock("teleporter_acacia_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_CHERRY_DOOR = registerBlock("teleporter_cherry_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_DARK_OAK_DOOR = registerBlock("teleporter_dark_oak_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_MANGROVE_DOOR = registerBlock("teleporter_mangrove_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.RED).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BAMBOO_DOOR = registerBlock("teleporter_bamboo_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_CRIMSON_DOOR = registerBlock("teleporter_crimson_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_WARPED_DOOR = registerBlock("teleporter_warped_door", new TeleporterDoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_OAK_TRAPDOOR = registerBlock("teleporter_oak_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_IRON_TRAPDOOR = registerBlock("teleporter_iron_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_SPRUCE_TRAPDOOR = registerBlock("teleporter_spruce_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BIRCH_TRAPDOOR = registerBlock("teleporter_birch_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_JUNGLE_TRAPDOOR = registerBlock("teleporter_jungle_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_ACACIA_TRAPDOOR = registerBlock("teleporter_acacia_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_CHERRY_TRAPDOOR = registerBlock("teleporter_cherry_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_DARK_OAK_TRAPDOOR = registerBlock("teleporter_dark_oak_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_MANGROVE_TRAPDOOR = registerBlock("teleporter_mangrove_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.RED).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BAMBOO_TRAPDOOR = registerBlock("teleporter_bamboo_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_CRIMSON_TRAPDOOR = registerBlock("teleporter_crimson_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block TELEPORTER_WARPED_TRAPDOOR = registerBlock("teleporter_warped_trapdoor", new TeleporterTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).strength(10.0F, 3600000.0f).nonOpaque()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block INTERACTIVE_TRIGGER_OAK_DOOR = registerBlock("interactive_trigger_oak_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_IRON_DOOR = registerBlock("interactive_trigger_iron_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_SPRUCE_DOOR = registerBlock("interactive_trigger_spruce_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BIRCH_DOOR = registerBlock("interactive_trigger_birch_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_JUNGLE_DOOR = registerBlock("interactive_trigger_jungle_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_ACACIA_DOOR = registerBlock("interactive_trigger_acacia_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_CHERRY_DOOR = registerBlock("interactive_trigger_cherry_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_DARK_OAK_DOOR = registerBlock("interactive_trigger_dark_oak_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_MANGROVE_DOOR = registerBlock("interactive_trigger_mangrove_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BAMBOO_DOOR = registerBlock("interactive_trigger_bamboo_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_CRIMSON_DOOR = registerBlock("interactive_trigger_crimson_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_WARPED_DOOR = registerBlock("interactive_trigger_warped_door", new InteractiveTriggerDoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_OAK_TRAPDOOR = registerBlock("interactive_trigger_oak_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_IRON_TRAPDOOR = registerBlock("interactive_trigger_iron_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_SPRUCE_TRAPDOOR = registerBlock("interactive_trigger_spruce_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BIRCH_TRAPDOOR = registerBlock("interactive_trigger_birch_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_JUNGLE_TRAPDOOR = registerBlock("interactive_trigger_jungle_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_ACACIA_TRAPDOOR = registerBlock("interactive_trigger_acacia_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_CHERRY_TRAPDOOR = registerBlock("interactive_trigger_cherry_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_DARK_OAK_TRAPDOOR = registerBlock("interactive_trigger_dark_oak_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_MANGROVE_TRAPDOOR = registerBlock("interactive_trigger_mangrove_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BAMBOO_TRAPDOOR = registerBlock("interactive_trigger_bamboo_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_CRIMSON_TRAPDOOR = registerBlock("interactive_trigger_crimson_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_WARPED_TRAPDOOR = registerBlock("interactive_trigger_warped_trapdoor", new InteractiveTriggerTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block INTERACTIVE_TRIGGER_CANDLE = registerBlock("interactive_trigger_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_WHITE_CANDLE = registerBlock("interactive_trigger_white_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.WHITE_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_ORANGE_CANDLE = registerBlock("interactive_trigger_orange_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_MAGENTA_CANDLE = registerBlock("interactive_trigger_magenta_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.MAGENTA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_LIGHT_BLUE_CANDLE = registerBlock("interactive_trigger_light_blue_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.LIGHT_BLUE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_YELLOW_CANDLE = registerBlock("interactive_trigger_yellow_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_LIME_CANDLE = registerBlock("interactive_trigger_lime_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.LIME).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_PINK_CANDLE = registerBlock("interactive_trigger_pink_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_GRAY_CANDLE = registerBlock("interactive_trigger_gray_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_LIGHT_GRAY_CANDLE = registerBlock("interactive_trigger_light_gray_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_CYAN_CANDLE = registerBlock("interactive_trigger_cyan_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.CYAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_PURPLE_CANDLE = registerBlock("interactive_trigger_purple_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.PURPLE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BLUE_CANDLE = registerBlock("interactive_trigger_blue_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.BLUE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BROWN_CANDLE = registerBlock("interactive_trigger_brown_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_GREEN_CANDLE = registerBlock("interactive_trigger_green_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.GREEN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_RED_CANDLE = registerBlock("interactive_trigger_red_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block INTERACTIVE_TRIGGER_BLACK_CANDLE = registerBlock("interactive_trigger_black_candle", new InteractiveTriggerCandleBlock(Block.Settings.create().mapColor(MapColor.BLACK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing().luminance(InteractiveTriggerCandleBlock.STATE_TO_LUMINANCE)), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block USE_RELAY_ANVIL = registerBlock("use_relay_anvil", new UseRelayAnvilBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block USE_RELAY_OAK_DOOR = registerBlock("use_relay_oak_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_IRON_DOOR = registerBlock("use_relay_iron_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_SPRUCE_DOOR = registerBlock("use_relay_spruce_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BIRCH_DOOR = registerBlock("use_relay_birch_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_JUNGLE_DOOR = registerBlock("use_relay_jungle_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_ACACIA_DOOR = registerBlock("use_relay_acacia_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CHERRY_DOOR = registerBlock("use_relay_cherry_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_DARK_OAK_DOOR = registerBlock("use_relay_dark_oak_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_MANGROVE_DOOR = registerBlock("use_relay_mangrove_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BAMBOO_DOOR = registerBlock("use_relay_bamboo_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CRIMSON_DOOR = registerBlock("use_relay_crimson_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_WARPED_DOOR = registerBlock("use_relay_warped_door", new UseRelayDoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_OAK_TRAPDOOR = registerBlock("use_relay_oak_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.OAK_TAN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_IRON_TRAPDOOR = registerBlock("use_relay_iron_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_SPRUCE_TRAPDOOR = registerBlock("use_relay_spruce_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.SPRUCE_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BIRCH_TRAPDOOR = registerBlock("use_relay_birch_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_JUNGLE_TRAPDOOR = registerBlock("use_relay_jungle_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DIRT_BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_ACACIA_TRAPDOOR = registerBlock("use_relay_acacia_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.ORANGE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CHERRY_TRAPDOOR = registerBlock("use_relay_cherry_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.TERRACOTTA_WHITE).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_DARK_OAK_TRAPDOOR = registerBlock("use_relay_dark_oak_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.BROWN).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_MANGROVE_TRAPDOOR = registerBlock("use_relay_mangrove_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.RED).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BAMBOO_TRAPDOOR = registerBlock("use_relay_bamboo_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.YELLOW).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_CRIMSON_TRAPDOOR = registerBlock("use_relay_crimson_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DULL_PINK).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block USE_RELAY_WARPED_TRAPDOOR = registerBlock("use_relay_warped_trapdoor", new UseRelayTrapdoorBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block USE_RELAY_CHEST = registerBlock("use_relay_chest", new UseRelayChestBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	public static final Block LOCKED_USE_RELAY_CHEST = registerBlock("locked_use_relay_chest", new UseRelayChestBlock(Block.Settings.create().mapColor(MapColor.DARK_AQUA).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block USE_RELAY_LECTERN = registerBlock("use_relay_lectern", new UseRelayLecternBlock(Block.Settings.create().mapColor(MapColor.IRON_GRAY).requiresTool().strength(-1.0f, 3600000.0f).nonOpaque().dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	@Deprecated
	public static final Block LOOTABLE_VAULT_BLOCK = registerBlock("lootable_vault_block", new LootableVaultBlock(Block.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).strength(50.0F).nonOpaque().sounds(BlockSoundGroup.VAULT).luminance(state -> ((LootableVaultState) state.get(LootableVaultBlock.LOOTABLE_VAULT_STATE)).getLuminance()).blockVision(Blocks::never).dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);

	public static final Block TRIGGERING_TRIAL_SPAWNER_BLOCK = registerBlock("triggering_trial_spawner_block", new TriggeringTrialSpawnerBlock(Block.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(NoteBlockInstrument.BASEDRUM).strength(50.0F).nonOpaque().sounds(BlockSoundGroup.TRIAL_SPAWNER).blockVision(Blocks::never).luminance(state -> ((TrialSpawnerState) state.get(TriggeringTrialSpawnerBlock.TRIAL_SPAWNER_STATE)).getLuminance()).blockVision(Blocks::never).dropsNothing()), ItemGroupRegistry.DECORATIVE_SCRIPT_BLOCKS);
	//endregion Content Blocks

	//region Script Blocks
	@Deprecated
	public static final Block AREA_BLOCK = registerBlock("area_block", new AreaBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), null);
	public static final Block AREA_FILLER_BLOCK = registerBlock("area_filler_block", new AreaFillerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block BOSS_CONTROLLER_BLOCK = registerBlock("boss_controller_block", new BossControllerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	//	public static final Block DATA_ACCESS_BLOCK = registerBlock("data_access_block", new DataAccessBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block COPY_DATA_BLOCK = registerBlock("copy_data_block", new CopyDataBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DATA_WRITING_BLOCK = registerBlock("data_writing_block", new DataWritingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DATA_RELAY_BLOCK = registerBlock("data_relay_block", new DataRelayBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DATA_SAVING_BLOCK = registerBlock("data_saving_block", new DataSavingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DELAY_TRIGGER_BLOCK = registerBlock("delay_trigger_block", new DelayTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block DIALOGUE_BLOCK = registerBlock("dialogue_block", new DialogueBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	@Deprecated
	public static final Block ENTRANCE_DELEGATION_BLOCK = registerBlock("entrance_delegation_block", new EntranceDelegationBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), null);
	public static final Block HOUSING_BLOCK = registerBlock("housing_block", new HousingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	@Deprecated
	public static final Block INTERACTIVE_LOOT_BLOCK = registerBlock("interactive_loot_block", new InteractiveLootBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), null);
	public static final Block JIGSAW_PLACER_BLOCK = registerBlock("jigsaw_placer_block", new JigsawPlacerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block LOCATION_CONTROL_BLOCK = registerBlock("location_control_block", new LocationControlBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block MIMIC_BLOCK = registerBlock("mimic_block", new MimicBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block MIMIC_FALLBACK_BLOCK = Registry.register(Registries.BLOCK, ScriptBlocks.identifier("mimic_fallback_block"), new Block(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()));
	public static final Block REDSTONE_TRIGGER_BLOCK = registerBlock("redstone_trigger_block", new RedstoneTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block PLAYER_DETECTOR_BLOCK = registerBlock("player_detector_block", new PlayerDetectorBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block PVP_CONTROLLER_BLOCK = registerBlock("pvp_controller_block", new PVPControllerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block RELAY_TRIGGER_BLOCK = registerBlock("relay_trigger_block", new RelayTriggerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block SHOP_BLOCK = registerBlock("shop_block", new ShopBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block SPAWN_POINT_DELEGATION_BLOCK = registerBlock("spawn_point_delegation_block", new SpawnPointDelegationBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TEAM_CONTROLLER_BLOCK = registerBlock("team_controller_block", new TeamControllerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TELEPORTER_BLOCK = registerBlock("teleporter_block", new TeleporterBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_ADVANCEMENT_CHECKER_BLOCK = registerBlock("triggered_advancement_checker_block", new TriggeredAdvancementCheckerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_BEACON_BLOCK = registerBlock("triggered_beacon_block", new TriggeredBeaconBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_COUNTER_BLOCK = registerBlock("triggered_counter_block", new TriggeredCounterBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_DAMAGE_DEALING_BLOCK = registerBlock("triggered_damage_dealing_block", new TriggeredDamageDealingBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_DISPENSER_BLOCK = registerBlock("triggered_dispenser_block", new TriggeredDispenserBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_DISPLAY_BLOCK = registerBlock("triggered_display_block", new TriggeredDisplayBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_ENTITY_REMOVER_BLOCK = registerBlock("triggered_entity_remover_block", new TriggeredEntityRemoverBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_REDSTONE_BLOCK = registerBlock("triggered_redstone_block", new TriggeredRedstoneBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_RNG_BLOCK = registerBlock("triggered_rng_block", new TriggeredRNGBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_SPAWNER_BLOCK = registerBlock("triggered_spawner_block", new TriggeredSpawnerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block TRIGGERED_VILLAGER_SPAWNER_BLOCK = registerBlock("triggered_villager_spawner_block", new TriggeredVillagerSpawnerBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing().nonOpaque()), ItemGroupRegistry.SCRIPT_BLOCKS);
	public static final Block USE_RELAY_BLOCK = registerBlock("use_relay_block", new UseRelayBlock(Block.Settings.create().mapColor(MapColor.LIGHT_GRAY).requiresTool().strength(-1.0f, 3600000.0f).dropsNothing()), ItemGroupRegistry.SCRIPT_BLOCKS);
	//endregion Script Blocks

	private static Block registerAestheticDecoratedPotBlock(String name, Block block, RegistryKey<ItemGroup> itemGroup) {
		Registry.register(Registries.ITEM, ScriptBlocks.identifier(name), new BlockItem(block, new Item.Settings().component(DataComponentTypes.POT_DECORATIONS, Sherds.DEFAULT)));
		ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(block));
		return Registry.register(Registries.BLOCK, ScriptBlocks.identifier(name), block);
	}

	private static Block registerBlock(String name, Block block, RegistryKey<ItemGroup> itemGroup) {
		Registry.register(Registries.ITEM, ScriptBlocks.identifier(name), new BlockItem(block, new Item.Settings()));
		ItemGroupEvents.modifyEntriesEvent(itemGroup).register(content -> content.add(block));
		return Registry.register(Registries.BLOCK, ScriptBlocks.identifier(name), block);
	}

	public static void init() {
	}
}
