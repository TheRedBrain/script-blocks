package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.entity.*;
import com.github.theredbrain.scriptblocks.entity.mob.BossEntity;
import com.github.theredbrain.scriptblocks.entity.mob.SpawnerBoundEntity;
import com.github.theredbrain.scriptblocks.entity.passive.SpawnerBoundVillagerEntity;
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

    public static final EntityType<BossEntity> BOSS_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            ScriptBlocksMod.identifier("boss_entity"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, BossEntity::new).dimensions(new EntityDimensions(0.6F, 1.8F, false)).build());
    public static final EntityType<SpawnerBoundEntity> SPAWNER_BOUND_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            ScriptBlocksMod.identifier("spawner_bound_entity"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpawnerBoundEntity::new).dimensions(new EntityDimensions(0.6F, 1.8F, false)).build());
    public static final EntityType<SpawnerBoundVillagerEntity> SPAWNER_BOUND_VILLAGER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            ScriptBlocksMod.identifier("spawner_bound_villager_entity"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SpawnerBoundVillagerEntity::new).dimensions(new EntityDimensions(0.6F, 1.95F, false)).build());

    //region Script Blocks
    public static final BlockEntityType<InteractiveLootBlockEntity> INTERACTIVE_LOOT_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("interactive_loot_block"),
            FabricBlockEntityTypeBuilder.create(InteractiveLootBlockEntity::new, BlockRegistry.INTERACTIVE_LOOT_BLOCK).build());
    public static final BlockEntityType<TriggeredCounterBlockEntity> TRIGGERED_COUNTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("triggered_counter_block"),
            FabricBlockEntityTypeBuilder.create(TriggeredCounterBlockEntity::new, BlockRegistry.TRIGGERED_COUNTER_BLOCK).build());
    public static final BlockEntityType<DialogueBlockEntity> DIALOGUE_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("dialogue_block"),
            FabricBlockEntityTypeBuilder.create(DialogueBlockEntity::new, BlockRegistry.DIALOGUE_BLOCK).build());
    public static final BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("shop_block"),
            FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, BlockRegistry.SHOP_BLOCK).build());
    public static final BlockEntityType<MimicBlockEntity> MIMIC_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("mimic_block"),
            FabricBlockEntityTypeBuilder.create(MimicBlockEntity::new, BlockRegistry.MIMIC_BLOCK).build());
    public static final BlockEntityType<TriggeredSpawnerBlockEntity> TRIGGERED_SPAWNER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("triggered_spawner_block"),
            FabricBlockEntityTypeBuilder.create(TriggeredSpawnerBlockEntity::new, BlockRegistry.TRIGGERED_SPAWNER_BLOCK).build());
    public static final BlockEntityType<LocationControlBlockEntity> LOCATION_CONTROL_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("location_control_block"),
            FabricBlockEntityTypeBuilder.create(LocationControlBlockEntity::new, BlockRegistry.LOCATION_CONTROL_BLOCK).build());
    public static final BlockEntityType<HousingBlockEntity> HOUSING_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("housing_block"),
            FabricBlockEntityTypeBuilder.create(HousingBlockEntity::new, BlockRegistry.HOUSING_BLOCK).build());
    public static final BlockEntityType<TeleporterBlockEntity> TELEPORTER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("teleporter_block"),
            FabricBlockEntityTypeBuilder.create(TeleporterBlockEntity::new, BlockRegistry.TELEPORTER_BLOCK).build());
    public static final BlockEntityType<JigsawPlacerBlockEntity> STRUCTURE_PLACER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("structure_placer_block"),
            FabricBlockEntityTypeBuilder.create(JigsawPlacerBlockEntity::new, BlockRegistry.JIGSAW_PLACER_BLOCK).build());
    public static final BlockEntityType<RedstoneTriggerBlockEntity> REDSTONE_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("redstone_trigger_block"),
            FabricBlockEntityTypeBuilder.create(RedstoneTriggerBlockEntity::new, BlockRegistry.REDSTONE_TRIGGER_BLOCK).build());
    public static final BlockEntityType<RelayTriggerBlockEntity> RELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("relay_trigger_block"),
            FabricBlockEntityTypeBuilder.create(RelayTriggerBlockEntity::new, BlockRegistry.RELAY_TRIGGER_BLOCK).build());
    public static final BlockEntityType<DelayTriggerBlockEntity> DELAY_TRIGGER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("delay_trigger_block"),
            FabricBlockEntityTypeBuilder.create(DelayTriggerBlockEntity::new, BlockRegistry.DELAY_TRIGGER_BLOCK).build());
    public static final BlockEntityType<EntranceDelegationBlockEntity> ENTRANCE_DELEGATION_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("entrance_delegation_block"),
            FabricBlockEntityTypeBuilder.create(EntranceDelegationBlockEntity::new, BlockRegistry.ENTRANCE_DELEGATION_BLOCK).build());
    public static final BlockEntityType<AreaBlockEntity> AREA_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("area_block"),
            FabricBlockEntityTypeBuilder.create(AreaBlockEntity::new, BlockRegistry.AREA_BLOCK).build());
    public static final BlockEntityType<BossControllerBlockEntity> BOSS_CONTROLLER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("boss_controller_block"),
            FabricBlockEntityTypeBuilder.create(BossControllerBlockEntity::new, BlockRegistry.BOSS_CONTROLLER_BLOCK).build());
    public static final BlockEntityType<TriggeredAdvancementCheckerBlockEntity> TRIGGERED_ADVANCEMENT_CHECKER_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("triggered_advancement_checker_block"),
            FabricBlockEntityTypeBuilder.create(TriggeredAdvancementCheckerBlockEntity::new, BlockRegistry.TRIGGERED_ADVANCEMENT_CHECKER_BLOCK).build());
    public static final BlockEntityType<UseRelayBlockEntity> USE_RELAY_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
            ScriptBlocksMod.identifier("use_relay_block"),
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
            ScriptBlocksMod.identifier("use_relay_chest_block"),
            FabricBlockEntityTypeBuilder.create(UseRelayChestBlockEntity::new,
                    BlockRegistry.USE_RELAY_CHEST,
                    BlockRegistry.LOCKED_USE_RELAY_CHEST).build());
    //endregion Script Blocks

    public static void init() {
    }

    public static void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(EntityRegistry.SPAWNER_BOUND_ENTITY, SpawnerBoundEntity.createLivingAttributes());
        FabricDefaultAttributeRegistry.register(EntityRegistry.SPAWNER_BOUND_VILLAGER_ENTITY, SpawnerBoundVillagerEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(EntityRegistry.BOSS_ENTITY, BossEntity.createLivingAttributes());
    }
}
