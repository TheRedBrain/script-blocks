package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataAccessBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityRegistry {

	//region Script Blocks
	public static final BlockEntityType<InteractiveLootBlockEntity> INTERACTIVE_LOOT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("interactive_loot_block"),
			FabricBlockEntityTypeBuilder.create(InteractiveLootBlockEntity::new, BlockRegistry.INTERACTIVE_LOOT_BLOCK).build());
	public static final BlockEntityType<TriggeredCounterBlockEntity> TRIGGERED_COUNTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_counter_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredCounterBlockEntity::new, BlockRegistry.TRIGGERED_COUNTER_BLOCK).build());
	public static final BlockEntityType<DataSavingBlockEntity> DATA_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_relay_block"),
			FabricBlockEntityTypeBuilder.create(DataSavingBlockEntity::new, BlockRegistry.DATA_RELAY_BLOCK).build());
	public static final BlockEntityType<DataSavingBlockEntity> DATA_SAVING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_saving_block"),
			FabricBlockEntityTypeBuilder.create(DataSavingBlockEntity::new, BlockRegistry.DATA_SAVING_BLOCK).build());
	public static final BlockEntityType<DataAccessBlockEntity> DATA_ACCESS_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_access_block"),
			FabricBlockEntityTypeBuilder.create(DataAccessBlockEntity::new, BlockRegistry.DATA_ACCESS_BLOCK).build());
	public static final BlockEntityType<DialogueBlockEntity> DIALOGUE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("dialogue_block"),
			FabricBlockEntityTypeBuilder.create(DialogueBlockEntity::new, BlockRegistry.DIALOGUE_BLOCK).build());
	public static final BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("shop_block"),
			FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, BlockRegistry.SHOP_BLOCK).build());
	public static final BlockEntityType<MimicBlockEntity> MIMIC_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("mimic_block"),
			FabricBlockEntityTypeBuilder.create(MimicBlockEntity::new, BlockRegistry.MIMIC_BLOCK).build());
	public static final BlockEntityType<TriggeredSpawnerBlockEntity> TRIGGERED_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_spawner_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredSpawnerBlockEntity::new, BlockRegistry.TRIGGERED_SPAWNER_BLOCK).build());
	public static final BlockEntityType<LocationControlBlockEntity> LOCATION_CONTROL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("location_control_block"),
			FabricBlockEntityTypeBuilder.create(LocationControlBlockEntity::new, BlockRegistry.LOCATION_CONTROL_BLOCK).build());
	public static final BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("housing_block"),
			FabricBlockEntityTypeBuilder.create(HousingBlockEntity::new, BlockRegistry.HOUSING_BLOCK).build());
	public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("teleporter_block"),
			FabricBlockEntityTypeBuilder.create(TeleporterBlockEntity::new, BlockRegistry.TELEPORTER_BLOCK).build());
	public static final BlockEntityType<JigsawPlacerBlockEntity> STRUCTURE_PLACER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("structure_placer_block"),
			FabricBlockEntityTypeBuilder.create(JigsawPlacerBlockEntity::new, BlockRegistry.JIGSAW_PLACER_BLOCK).build());
	public static final BlockEntityType<RedstoneTriggerBlockEntity> REDSTONE_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("redstone_trigger_block"),
			FabricBlockEntityTypeBuilder.create(RedstoneTriggerBlockEntity::new, BlockRegistry.REDSTONE_TRIGGER_BLOCK).build());
	public static final BlockEntityType<RelayTriggerBlockEntity> RELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("relay_trigger_block"),
			FabricBlockEntityTypeBuilder.create(RelayTriggerBlockEntity::new, BlockRegistry.RELAY_TRIGGER_BLOCK).build());
	public static final BlockEntityType<DelayTriggerBlockEntity> DELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("delay_trigger_block"),
			FabricBlockEntityTypeBuilder.create(DelayTriggerBlockEntity::new, BlockRegistry.DELAY_TRIGGER_BLOCK).build());
	public static final BlockEntityType<EntranceDelegationBlockEntity> ENTRANCE_DELEGATION_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("entrance_delegation_block"),
			FabricBlockEntityTypeBuilder.create(EntranceDelegationBlockEntity::new, BlockRegistry.ENTRANCE_DELEGATION_BLOCK).build());
	public static final BlockEntityType<AreaBlockEntity> AREA_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("area_block"),
			FabricBlockEntityTypeBuilder.create(AreaBlockEntity::new, BlockRegistry.AREA_BLOCK).build());
	public static final BlockEntityType<BossControllerBlockEntity> BOSS_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("boss_controller_block"),
			FabricBlockEntityTypeBuilder.create(BossControllerBlockEntity::new, BlockRegistry.BOSS_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<TriggeredAdvancementCheckerBlockEntity> TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_advancement_checker_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredAdvancementCheckerBlockEntity::new, BlockRegistry.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK).build());
	public static final BlockEntityType<UseRelayBlockEntity> USE_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_block"),
			FabricBlockEntityTypeBuilder.create(UseRelayBlockEntity::new,
					BlockRegistry.USE_RELAY_BLOCK,
					BlockRegistry.USE_RELAY_OAK_DOOR,
					BlockRegistry.USE_RELAY_IRON_DOOR,
					BlockRegistry.USE_RELAY_SPRUCE_DOOR,
					BlockRegistry.USE_RELAY_BIRCH_DOOR,
					BlockRegistry.USE_RELAY_JUNGLE_DOOR,
					BlockRegistry.USE_RELAY_ACACIA_DOOR,
					BlockRegistry.USE_RELAY_CHERRY_DOOR,
					BlockRegistry.USE_RELAY_DARK_OAK_DOOR,
					BlockRegistry.USE_RELAY_MANGROVE_DOOR,
					BlockRegistry.USE_RELAY_BAMBOO_DOOR,
					BlockRegistry.USE_RELAY_CRIMSON_DOOR,
					BlockRegistry.USE_RELAY_WARPED_DOOR,
					BlockRegistry.USE_RELAY_OAK_TRAPDOOR,
					BlockRegistry.USE_RELAY_IRON_TRAPDOOR,
					BlockRegistry.USE_RELAY_SPRUCE_TRAPDOOR,
					BlockRegistry.USE_RELAY_BIRCH_TRAPDOOR,
					BlockRegistry.USE_RELAY_JUNGLE_TRAPDOOR,
					BlockRegistry.USE_RELAY_ACACIA_TRAPDOOR,
					BlockRegistry.USE_RELAY_CHERRY_TRAPDOOR,
					BlockRegistry.USE_RELAY_DARK_OAK_TRAPDOOR,
					BlockRegistry.USE_RELAY_MANGROVE_TRAPDOOR,
					BlockRegistry.USE_RELAY_BAMBOO_TRAPDOOR,
					BlockRegistry.USE_RELAY_CRIMSON_TRAPDOOR,
					BlockRegistry.USE_RELAY_WARPED_TRAPDOOR).build());
	public static final BlockEntityType<UseRelayChestBlockEntity> USE_RELAY_CHEST_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_chest_block"),
			FabricBlockEntityTypeBuilder.create(UseRelayChestBlockEntity::new,
					BlockRegistry.USE_RELAY_CHEST,
					BlockRegistry.LOCKED_USE_RELAY_CHEST).build());
	//endregion Script Blocks

	public static void init() {
	}
}
