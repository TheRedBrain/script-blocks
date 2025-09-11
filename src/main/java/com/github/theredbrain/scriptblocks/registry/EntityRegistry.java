package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDispenserBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredEntityRemoverBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredRedstoneBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeringTrialSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayLecternBlockEntity;
import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityRegistry {

	//region Script Blocks
	public static final BlockEntityType<LootableVaultBlockEntity> LOOTABLE_VAULT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("lootable_vault_block"),
			FabricBlockEntityTypeBuilder.create(LootableVaultBlockEntity::new, BlockRegistry.LOOTABLE_VAULT_BLOCK).build());
	public static final BlockEntityType<TriggeringTrialSpawnerBlockEntity> TRIGGERING_TRIAL_SPAWNER = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggering_trial_spawner_block"),
			FabricBlockEntityTypeBuilder.create(TriggeringTrialSpawnerBlockEntity::new, BlockRegistry.TRIGGERING_TRIAL_SPAWNER_BLOCK).build());
	public static final BlockEntityType<AestheticDecoratedPotBlockEntity> AESTHETIC_DECORATED_POT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("aesthetic_decorated_pot"),
			FabricBlockEntityTypeBuilder.create(AestheticDecoratedPotBlockEntity::new, BlockRegistry.AESTHETIC_DECORATED_POT).build());
	public static final BlockEntityType<TriggeredDispenserBlockEntity> TRIGGERED_DISPENSER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_dispenser_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredDispenserBlockEntity::new, BlockRegistry.TRIGGERED_DISPENSER_BLOCK).build());
	public static final BlockEntityType<TriggeredBeaconBlockEntity> TRIGGERED_BEACON_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_beacon_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredBeaconBlockEntity::new, BlockRegistry.TRIGGERED_BEACON_BLOCK).build());
	public static final BlockEntityType<TriggeredDisplayBlockEntity> TRIGGERED_DISPLAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_display_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredDisplayBlockEntity::new, BlockRegistry.TRIGGERED_DISPLAY_BLOCK).build());
	public static final BlockEntityType<TriggeredEntityRemoverBlockEntity> TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_entity_remover_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredEntityRemoverBlockEntity::new, BlockRegistry.TRIGGERED_ENTITY_REMOVER_BLOCK).build());
	public static final BlockEntityType<TriggeredRNGBlockEntity> TRIGGERED_RNG_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_rng_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredRNGBlockEntity::new, BlockRegistry.TRIGGERED_RNG_BLOCK).build());
	public static final BlockEntityType<InteractiveLootBlockEntity> INTERACTIVE_LOOT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("interactive_loot_block"),
			FabricBlockEntityTypeBuilder.create(InteractiveLootBlockEntity::new, BlockRegistry.INTERACTIVE_LOOT_BLOCK).build());
	public static final BlockEntityType<TriggeredCounterBlockEntity> TRIGGERED_COUNTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_counter_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredCounterBlockEntity::new, BlockRegistry.TRIGGERED_COUNTER_BLOCK).build());
	public static final BlockEntityType<CopyDataBlockEntity> COPY_DATA_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("copy_data_block"),
			FabricBlockEntityTypeBuilder.create(CopyDataBlockEntity::new, BlockRegistry.COPY_DATA_BLOCK).build());
	public static final BlockEntityType<DataRelayBlockEntity> DATA_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_relay_block"),
			FabricBlockEntityTypeBuilder.create(DataRelayBlockEntity::new, BlockRegistry.DATA_RELAY_BLOCK).build());
	public static final BlockEntityType<DataSavingBlockEntity> DATA_SAVING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_saving_block"),
			FabricBlockEntityTypeBuilder.create(DataSavingBlockEntity::new, BlockRegistry.DATA_SAVING_BLOCK).build());
	//	public static final BlockEntityType<DataAccessBlockEntity> DATA_ACCESS_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
//			ScriptBlocks.identifier("data_access_block"),
//			FabricBlockEntityTypeBuilder.create(DataAccessBlockEntity::new, BlockRegistry.DATA_ACCESS_BLOCK).build());
	public static final BlockEntityType<DataWritingBlockEntity> DATA_WRITING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_writing_block"),
			FabricBlockEntityTypeBuilder.create(DataWritingBlockEntity::new, BlockRegistry.DATA_WRITING_BLOCK).build());
	public static final BlockEntityType<DialogueBlockEntity> DIALOGUE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("dialogue_block"),
			FabricBlockEntityTypeBuilder.create(DialogueBlockEntity::new, BlockRegistry.DIALOGUE_BLOCK).build());
	public static final BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("shop_block"),
			FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, BlockRegistry.SHOP_BLOCK).build());
	public static final BlockEntityType<MimicBlockEntity> MIMIC_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("mimic_block"),
			FabricBlockEntityTypeBuilder.create(MimicBlockEntity::new, BlockRegistry.MIMIC_BLOCK).build());
	public static final BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("player_detector_block"),
			FabricBlockEntityTypeBuilder.create(PlayerDetectorBlockEntity::new, BlockRegistry.PLAYER_DETECTOR_BLOCK).build());
	public static final BlockEntityType<TriggeredSpawnerBlockEntity> TRIGGERED_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_spawner_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredSpawnerBlockEntity::new, BlockRegistry.TRIGGERED_SPAWNER_BLOCK).build());
	public static final BlockEntityType<TriggeredVillagerSpawnerBlockEntity> TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_villager_spawner_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredVillagerSpawnerBlockEntity::new, BlockRegistry.TRIGGERED_VILLAGER_SPAWNER_BLOCK).build());
	public static final BlockEntityType<LocationControlBlockEntity> LOCATION_CONTROL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("location_control_block"),
			FabricBlockEntityTypeBuilder.create(LocationControlBlockEntity::new, BlockRegistry.LOCATION_CONTROL_BLOCK).build());
	public static final BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("housing_block"),
			FabricBlockEntityTypeBuilder.create(HousingBlockEntity::new, BlockRegistry.HOUSING_BLOCK).build());
	public static final BlockEntityType<TeamControllerBlockEntity> TEAM_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("team_controller_block"),
			FabricBlockEntityTypeBuilder.create(TeamControllerBlockEntity::new, BlockRegistry.TEAM_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<PVPControllerBlockEntity> PVP_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("pvp_controller_block"),
			FabricBlockEntityTypeBuilder.create(PVPControllerBlockEntity::new, BlockRegistry.PVP_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("teleporter_block"),
			FabricBlockEntityTypeBuilder.create(TeleporterBlockEntity::new,
					BlockRegistry.TELEPORTER_BLOCK,
					BlockRegistry.TELEPORTER_OAK_DOOR,
					BlockRegistry.TELEPORTER_IRON_DOOR,
					BlockRegistry.TELEPORTER_SPRUCE_DOOR,
					BlockRegistry.TELEPORTER_BIRCH_DOOR,
					BlockRegistry.TELEPORTER_JUNGLE_DOOR,
					BlockRegistry.TELEPORTER_ACACIA_DOOR,
					BlockRegistry.TELEPORTER_CHERRY_DOOR,
					BlockRegistry.TELEPORTER_DARK_OAK_DOOR,
					BlockRegistry.TELEPORTER_MANGROVE_DOOR,
					BlockRegistry.TELEPORTER_BAMBOO_DOOR,
					BlockRegistry.TELEPORTER_CRIMSON_DOOR,
					BlockRegistry.TELEPORTER_WARPED_DOOR,
					BlockRegistry.TELEPORTER_OAK_TRAPDOOR,
					BlockRegistry.TELEPORTER_IRON_TRAPDOOR,
					BlockRegistry.TELEPORTER_SPRUCE_TRAPDOOR,
					BlockRegistry.TELEPORTER_BIRCH_TRAPDOOR,
					BlockRegistry.TELEPORTER_JUNGLE_TRAPDOOR,
					BlockRegistry.TELEPORTER_ACACIA_TRAPDOOR,
					BlockRegistry.TELEPORTER_CHERRY_TRAPDOOR,
					BlockRegistry.TELEPORTER_DARK_OAK_TRAPDOOR,
					BlockRegistry.TELEPORTER_MANGROVE_TRAPDOOR,
					BlockRegistry.TELEPORTER_BAMBOO_TRAPDOOR,
					BlockRegistry.TELEPORTER_CRIMSON_TRAPDOOR,
					BlockRegistry.TELEPORTER_WARPED_TRAPDOOR).build());
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
	public static final BlockEntityType<AreaFillerBlockEntity> AREA_FILLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("area_filler_block"),
			FabricBlockEntityTypeBuilder.create(AreaFillerBlockEntity::new, BlockRegistry.AREA_FILLER_BLOCK).build());
	public static final BlockEntityType<BossControllerBlockEntity> BOSS_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("boss_controller_block"),
			FabricBlockEntityTypeBuilder.create(BossControllerBlockEntity::new, BlockRegistry.BOSS_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<TriggeredRedstoneBlockEntity> TRIGGERED_REDSTONE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_redstone_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredRedstoneBlockEntity::new, BlockRegistry.TRIGGERED_REDSTONE_BLOCK).build());
	public static final BlockEntityType<TriggeredAdvancementCheckerBlockEntity> TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_advancement_checker_block"),
			FabricBlockEntityTypeBuilder.create(TriggeredAdvancementCheckerBlockEntity::new, BlockRegistry.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK).build());
	public static final BlockEntityType<UseRelayBlockEntity> USE_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_block"),
			FabricBlockEntityTypeBuilder.create(UseRelayBlockEntity::new,
					BlockRegistry.USE_RELAY_BLOCK,
					BlockRegistry.USE_RELAY_ANVIL,
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
	public static final BlockEntityType<UseRelayLecternBlockEntity> USE_RELAY_LECTERN_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_lectern"),
			FabricBlockEntityTypeBuilder.create(UseRelayLecternBlockEntity::new,
					BlockRegistry.USE_RELAY_LECTERN).build());
	//endregion Script Blocks

	public static final EntityType<FakeVillagerEntity> FAKE_VILLAGER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
			ScriptBlocks.identifier("fake_villager"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, FakeVillagerEntity::new).dimensions(EntityDimensions.changing(0.6F, 1.95F)).build());

	public static void init() {
		registerEntityAttributes();
	}

	public static void registerEntityAttributes() {
		FabricDefaultAttributeRegistry.register(EntityRegistry.FAKE_VILLAGER_ENTITY, FakeVillagerEntity.createMobAttributes());
	}
}
