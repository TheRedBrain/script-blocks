package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.BossControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataWritingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DialogueBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.EntranceDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveLootBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.JigsawPlacerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LocationControlBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PVPControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.PlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.SpawnPointDelegationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredBeaconBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDamageDealingBlockEntity;
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
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ScriptBlocksEntities {

	//region Script Blocks
	@Deprecated
	public static final BlockEntityType<LootableVaultBlockEntity> LOOTABLE_VAULT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("lootable_vault_block"),
			BlockEntityType.Builder.create(LootableVaultBlockEntity::new, ScriptBlocksBlocks.LOOTABLE_VAULT_BLOCK).build());
	public static final BlockEntityType<TriggeringTrialSpawnerBlockEntity> TRIGGERING_TRIAL_SPAWNER = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggering_trial_spawner_block"),
			BlockEntityType.Builder.create(TriggeringTrialSpawnerBlockEntity::new, ScriptBlocksBlocks.TRIGGERING_TRIAL_SPAWNER_BLOCK).build());
	public static final BlockEntityType<AestheticDecoratedPotBlockEntity> AESTHETIC_DECORATED_POT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("aesthetic_decorated_pot"),
			BlockEntityType.Builder.create(AestheticDecoratedPotBlockEntity::new, ScriptBlocksBlocks.AESTHETIC_DECORATED_POT).build());
	public static final BlockEntityType<TriggeredDispenserBlockEntity> TRIGGERED_DISPENSER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_dispenser_block"),
			BlockEntityType.Builder.create(TriggeredDispenserBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_DISPENSER_BLOCK).build());
	public static final BlockEntityType<TriggeredBeaconBlockEntity> TRIGGERED_BEACON_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_beacon_block"),
			BlockEntityType.Builder.create(TriggeredBeaconBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_BEACON_BLOCK).build());
	public static final BlockEntityType<TriggeredDisplayBlockEntity> TRIGGERED_DISPLAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_display_block"),
			BlockEntityType.Builder.create(TriggeredDisplayBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_DISPLAY_BLOCK).build());
	public static final BlockEntityType<TriggeredDamageDealingBlockEntity> TRIGGERED_DAMAGE_DEALING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_damage_dealing_block"),
			BlockEntityType.Builder.create(TriggeredDamageDealingBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_DAMAGE_DEALING_BLOCK).build());
	public static final BlockEntityType<TriggeredEntityRemoverBlockEntity> TRIGGERED_ENTITY_REMOVER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_entity_remover_block"),
			BlockEntityType.Builder.create(TriggeredEntityRemoverBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_ENTITY_REMOVER_BLOCK).build());
	public static final BlockEntityType<TriggeredRNGBlockEntity> TRIGGERED_RNG_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_rng_block"),
			BlockEntityType.Builder.create(TriggeredRNGBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_RNG_BLOCK).build());
	@Deprecated
	public static final BlockEntityType<InteractiveLootBlockEntity> INTERACTIVE_LOOT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("interactive_loot_block"),
			BlockEntityType.Builder.create(InteractiveLootBlockEntity::new, ScriptBlocksBlocks.INTERACTIVE_LOOT_BLOCK).build());
	public static final BlockEntityType<TriggeredCounterBlockEntity> TRIGGERED_COUNTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_counter_block"),
			BlockEntityType.Builder.create(TriggeredCounterBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_COUNTER_BLOCK).build());
	public static final BlockEntityType<CopyDataBlockEntity> COPY_DATA_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("copy_data_block"),
			BlockEntityType.Builder.create(CopyDataBlockEntity::new, ScriptBlocksBlocks.COPY_DATA_BLOCK).build());
	public static final BlockEntityType<DataModificationBlockEntity> DATA_MODIFICATION_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_modification_block"),
			BlockEntityType.Builder.create(DataModificationBlockEntity::new, ScriptBlocksBlocks.DATA_MODIFICATION_BLOCK).build());
	public static final BlockEntityType<DataRelayBlockEntity> DATA_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_relay_block"),
			BlockEntityType.Builder.create(DataRelayBlockEntity::new, ScriptBlocksBlocks.DATA_RELAY_BLOCK).build());
	public static final BlockEntityType<DataSavingBlockEntity> DATA_SAVING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_saving_block"),
			BlockEntityType.Builder.create(DataSavingBlockEntity::new, ScriptBlocksBlocks.DATA_SAVING_BLOCK).build());
	//	public static final BlockEntityType<DataAccessBlockEntity> DATA_ACCESS_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
//			ScriptBlocks.identifier("data_access_block"),
//			BlockEntityType.Builder.create(DataAccessBlockEntity::new, BlockRegistry.DATA_ACCESS_BLOCK).build());
	public static final BlockEntityType<DataWritingBlockEntity> DATA_WRITING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("data_writing_block"),
			BlockEntityType.Builder.create(DataWritingBlockEntity::new, ScriptBlocksBlocks.DATA_WRITING_BLOCK).build());
	public static final BlockEntityType<DialogueBlockEntity> DIALOGUE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("dialogue_block"),
			BlockEntityType.Builder.create(DialogueBlockEntity::new, ScriptBlocksBlocks.DIALOGUE_BLOCK).build());
	public static final BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("shop_block"),
			BlockEntityType.Builder.create(ShopBlockEntity::new, ScriptBlocksBlocks.SHOP_BLOCK).build());
	public static final BlockEntityType<MimicBlockEntity> MIMIC_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("mimic_block"),
			BlockEntityType.Builder.create(MimicBlockEntity::new, ScriptBlocksBlocks.MIMIC_BLOCK).build());
	public static final BlockEntityType<PlayerDetectorBlockEntity> PLAYER_DETECTOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("player_detector_block"),
			BlockEntityType.Builder.create(PlayerDetectorBlockEntity::new, ScriptBlocksBlocks.PLAYER_DETECTOR_BLOCK).build());
	public static final BlockEntityType<TriggeredSpawnerBlockEntity> TRIGGERED_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_spawner_block"),
			BlockEntityType.Builder.create(TriggeredSpawnerBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_SPAWNER_BLOCK).build());
	public static final BlockEntityType<TriggeredVillagerSpawnerBlockEntity> TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_villager_spawner_block"),
			BlockEntityType.Builder.create(TriggeredVillagerSpawnerBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_VILLAGER_SPAWNER_BLOCK).build());
	public static final BlockEntityType<LocationControlBlockEntity> LOCATION_CONTROL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("location_control_block"),
			BlockEntityType.Builder.create(LocationControlBlockEntity::new, ScriptBlocksBlocks.LOCATION_CONTROL_BLOCK).build());
	public static final BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("housing_block"),
			BlockEntityType.Builder.create(HousingBlockEntity::new, ScriptBlocksBlocks.HOUSING_BLOCK).build());
	public static final BlockEntityType<TeamControllerBlockEntity> TEAM_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("team_controller_block"),
			BlockEntityType.Builder.create(TeamControllerBlockEntity::new, ScriptBlocksBlocks.TEAM_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<PVPControllerBlockEntity> PVP_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("pvp_controller_block"),
			BlockEntityType.Builder.create(PVPControllerBlockEntity::new, ScriptBlocksBlocks.PVP_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("teleporter_block"),
			BlockEntityType.Builder.create(TeleporterBlockEntity::new,
					ScriptBlocksBlocks.TELEPORTER_BLOCK,
					ScriptBlocksBlocks.TELEPORTER_OAK_DOOR,
					ScriptBlocksBlocks.TELEPORTER_IRON_DOOR,
					ScriptBlocksBlocks.TELEPORTER_SPRUCE_DOOR,
					ScriptBlocksBlocks.TELEPORTER_BIRCH_DOOR,
					ScriptBlocksBlocks.TELEPORTER_JUNGLE_DOOR,
					ScriptBlocksBlocks.TELEPORTER_ACACIA_DOOR,
					ScriptBlocksBlocks.TELEPORTER_CHERRY_DOOR,
					ScriptBlocksBlocks.TELEPORTER_DARK_OAK_DOOR,
					ScriptBlocksBlocks.TELEPORTER_MANGROVE_DOOR,
					ScriptBlocksBlocks.TELEPORTER_BAMBOO_DOOR,
					ScriptBlocksBlocks.TELEPORTER_CRIMSON_DOOR,
					ScriptBlocksBlocks.TELEPORTER_WARPED_DOOR,
					ScriptBlocksBlocks.TELEPORTER_OAK_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_IRON_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_SPRUCE_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_BIRCH_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_JUNGLE_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_ACACIA_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_CHERRY_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_DARK_OAK_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_MANGROVE_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_BAMBOO_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_CRIMSON_TRAPDOOR,
					ScriptBlocksBlocks.TELEPORTER_WARPED_TRAPDOOR).build());
	public static final BlockEntityType<JigsawPlacerBlockEntity> JIGSAW_PLACER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("jigsaw_placer_block"),
			BlockEntityType.Builder.create(JigsawPlacerBlockEntity::new, ScriptBlocksBlocks.JIGSAW_PLACER_BLOCK).build());
	public static final BlockEntityType<RedstoneTriggerBlockEntity> REDSTONE_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("redstone_trigger_block"),
			BlockEntityType.Builder.create(RedstoneTriggerBlockEntity::new, ScriptBlocksBlocks.REDSTONE_TRIGGER_BLOCK).build());
	public static final BlockEntityType<InteractiveTriggerBlockEntity> INTERACTIVE_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("interactive_trigger_block"),
			BlockEntityType.Builder.create(InteractiveTriggerBlockEntity::new,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OAK_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_IRON_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SPRUCE_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BIRCH_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_JUNGLE_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_ACACIA_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CHERRY_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_DARK_OAK_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_MANGROVE_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BAMBOO_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CRIMSON_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WARPED_DOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_OAK_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_IRON_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_SPRUCE_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BIRCH_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_JUNGLE_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_ACACIA_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CHERRY_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_DARK_OAK_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_MANGROVE_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BAMBOO_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CRIMSON_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WARPED_TRAPDOOR,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_WHITE_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_ORANGE_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_MAGENTA_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_LIGHT_BLUE_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_YELLOW_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_LIME_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_PINK_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_GRAY_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_LIGHT_GRAY_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_CYAN_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_PURPLE_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BLUE_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BROWN_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_GREEN_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_RED_CANDLE,
					ScriptBlocksBlocks.INTERACTIVE_TRIGGER_BLACK_CANDLE
			).build());
	public static final BlockEntityType<RelayTriggerBlockEntity> RELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("relay_trigger_block"),
			BlockEntityType.Builder.create(RelayTriggerBlockEntity::new, ScriptBlocksBlocks.RELAY_TRIGGER_BLOCK).build());
	public static final BlockEntityType<DelayTriggerBlockEntity> DELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("delay_trigger_block"),
			BlockEntityType.Builder.create(DelayTriggerBlockEntity::new, ScriptBlocksBlocks.DELAY_TRIGGER_BLOCK).build());
	@Deprecated
	public static final BlockEntityType<EntranceDelegationBlockEntity> ENTRANCE_DELEGATION_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("entrance_delegation_block"),
			BlockEntityType.Builder.create(EntranceDelegationBlockEntity::new, ScriptBlocksBlocks.ENTRANCE_DELEGATION_BLOCK).build());
	public static final BlockEntityType<SpawnPointDelegationBlockEntity> SPAWN_POINT_DELEGATION_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("spawn_point_delegation_block"),
			BlockEntityType.Builder.create(SpawnPointDelegationBlockEntity::new, ScriptBlocksBlocks.SPAWN_POINT_DELEGATION_BLOCK).build());
	@Deprecated
	public static final BlockEntityType<AreaBlockEntity> AREA_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("area_block"),
			BlockEntityType.Builder.create(AreaBlockEntity::new, ScriptBlocksBlocks.AREA_BLOCK).build());
	public static final BlockEntityType<AreaFillerBlockEntity> AREA_FILLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("area_filler_block"),
			BlockEntityType.Builder.create(AreaFillerBlockEntity::new, ScriptBlocksBlocks.AREA_FILLER_BLOCK).build());
	public static final BlockEntityType<BossControllerBlockEntity> BOSS_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("boss_controller_block"),
			BlockEntityType.Builder.create(BossControllerBlockEntity::new, ScriptBlocksBlocks.BOSS_CONTROLLER_BLOCK).build());
	public static final BlockEntityType<TriggeredRedstoneBlockEntity> TRIGGERED_REDSTONE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_redstone_block"),
			BlockEntityType.Builder.create(TriggeredRedstoneBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_REDSTONE_BLOCK).build());
	public static final BlockEntityType<TriggeredAdvancementCheckerBlockEntity> TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("triggered_advancement_checker_block"),
			BlockEntityType.Builder.create(TriggeredAdvancementCheckerBlockEntity::new, ScriptBlocksBlocks.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK).build());
	public static final BlockEntityType<UseRelayBlockEntity> USE_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_block"),
			BlockEntityType.Builder.create(UseRelayBlockEntity::new,
					ScriptBlocksBlocks.USE_RELAY_BLOCK,
					ScriptBlocksBlocks.USE_RELAY_ANVIL,
					ScriptBlocksBlocks.USE_RELAY_OAK_DOOR,
					ScriptBlocksBlocks.USE_RELAY_IRON_DOOR,
					ScriptBlocksBlocks.USE_RELAY_SPRUCE_DOOR,
					ScriptBlocksBlocks.USE_RELAY_BIRCH_DOOR,
					ScriptBlocksBlocks.USE_RELAY_JUNGLE_DOOR,
					ScriptBlocksBlocks.USE_RELAY_ACACIA_DOOR,
					ScriptBlocksBlocks.USE_RELAY_CHERRY_DOOR,
					ScriptBlocksBlocks.USE_RELAY_DARK_OAK_DOOR,
					ScriptBlocksBlocks.USE_RELAY_MANGROVE_DOOR,
					ScriptBlocksBlocks.USE_RELAY_BAMBOO_DOOR,
					ScriptBlocksBlocks.USE_RELAY_CRIMSON_DOOR,
					ScriptBlocksBlocks.USE_RELAY_WARPED_DOOR,
					ScriptBlocksBlocks.USE_RELAY_OAK_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_IRON_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_SPRUCE_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_BIRCH_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_JUNGLE_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_ACACIA_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_CHERRY_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_DARK_OAK_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_MANGROVE_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_BAMBOO_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_CRIMSON_TRAPDOOR,
					ScriptBlocksBlocks.USE_RELAY_WARPED_TRAPDOOR).build());
	public static final BlockEntityType<UseRelayChestBlockEntity> USE_RELAY_CHEST_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_chest_block"),
			BlockEntityType.Builder.create(UseRelayChestBlockEntity::new,
					ScriptBlocksBlocks.TRAPPED_USE_RELAY_CHEST,
					ScriptBlocksBlocks.USE_RELAY_CHEST,
					ScriptBlocksBlocks.LOCKED_USE_RELAY_CHEST).build());
	public static final BlockEntityType<UseRelayLecternBlockEntity> USE_RELAY_LECTERN_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
			ScriptBlocks.identifier("use_relay_lectern"),
			BlockEntityType.Builder.create(UseRelayLecternBlockEntity::new,
					ScriptBlocksBlocks.USE_RELAY_LECTERN).build());
	//endregion Script Blocks

	public static final EntityType<FakeVillagerEntity> FAKE_VILLAGER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
			ScriptBlocks.identifier("fake_villager"),
			EntityType.Builder.create(FakeVillagerEntity::new, SpawnGroup.MISC).dimensions(0.6F, 1.95F).build());

	public static void init() {
		registerEntityAttributes();
	}

	public static void registerEntityAttributes() {
		FabricDefaultAttributeRegistry.register(ScriptBlocksEntities.FAKE_VILLAGER_ENTITY, FakeVillagerEntity.createMobAttributes());
	}
}
