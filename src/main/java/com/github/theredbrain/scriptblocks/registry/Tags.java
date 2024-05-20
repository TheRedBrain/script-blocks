package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class Tags {
    //region EntityTags
//    public static final TagKey<EntityType<?>> ATTACKS_WITH_BASHING = TagKey.of(RegistryKeys.ENTITY_TYPE, ScriptBlocksMod.identifier("attacks_with_bashing"));
//    public static final TagKey<EntityType<?>> ATTACKS_WITH_PIERCING = TagKey.of(RegistryKeys.ENTITY_TYPE, ScriptBlocksMod.identifier("attacks_with_piercing"));
//    public static final TagKey<EntityType<?>> ATTACKS_WITH_SLASHING = TagKey.of(RegistryKeys.ENTITY_TYPE, ScriptBlocksMod.identifier("attacks_with_slashing"));
//    //endregion EntityTags
//    //region BlockTags
//    public static final TagKey<Block> PROVIDES_CRAFTING_TAB_0_LEVEL = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_crafting_tab_0_level"));
//    public static final TagKey<Block> PROVIDES_CRAFTING_TAB_1_LEVEL = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_crafting_tab_1_level"));
//    public static final TagKey<Block> PROVIDES_CRAFTING_TAB_2_LEVEL = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_crafting_tab_2_level"));
//    public static final TagKey<Block> PROVIDES_CRAFTING_TAB_3_LEVEL = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_crafting_tab_3_level"));
//    public static final TagKey<Block> PROVIDES_STORAGE_AREA_0 = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_storage_area_0"));
//    public static final TagKey<Block> PROVIDES_STORAGE_AREA_1 = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_storage_area_1"));
//    public static final TagKey<Block> PROVIDES_STORAGE_AREA_2 = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_storage_area_2"));
//    public static final TagKey<Block> PROVIDES_STORAGE_AREA_3 = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_storage_area_3"));
//    public static final TagKey<Block> PROVIDES_STORAGE_AREA_4 = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("provides_storage_area_4"));
//    public static final TagKey<Block> SLOWS_DOWN_ENTITIES_INSIDE = TagKey.of(RegistryKeys.BLOCK, ScriptBlocksMod.identifier("slows_down_entities_inside"));
//    //endregion BlockTags
//    //region DamageTypeTags
//    public static final TagKey<DamageType> IS_VANILLA = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("is_vanilla"));
//    public static final TagKey<DamageType> IS_TRUE_DAMAGE = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("is_true_damage"));
//
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_BASHING_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_bashing_division_of_1"));
//
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_PIERCING_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_piercing_division_of_1"));
//
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_SLASHING_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_slashing_division_of_1"));
//
//    public static final TagKey<DamageType> APPLIES_BLEEDING = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("applies_bleeding"));
//
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_POISON_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_poison_division_of_1"));
//
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_FIRE_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_fire_division_of_1"));
//
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_FROST_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_frost_division_of_1"));
//
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_1"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_2 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_2"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_3 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_3"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_4 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_4"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_5 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_5"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_6 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_6"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_7 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_7"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_8 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_8"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_0_9 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_0_9"));
//    public static final TagKey<DamageType> HAS_LIGHTNING_DIVISION_OF_1 = TagKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocksMod.identifier("has_lightning_division_of_1"));

    //endregion DamageTypeTags
    //region ItemTags
    public static final TagKey<Item> INTERACTIVE_STONE_BLOCK_TOOLS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("tools/interactive_stone_block_tools"));
    public static final TagKey<Item> INTERACTIVE_OAK_LOG_TOOLS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("tools/interactive_oak_log_tools"));
    public static final TagKey<Item> KEYS_FOR_LOCKED_USE_RELAY_CHEST = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("keys_for_locked_use_relay_chest"));
//    public static final TagKey<Item> ADVENTURE_HOTBAR_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("adventure_hotbar_items"));
//    public static final TagKey<Item> ATTACK_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("attack_items"));
//    public static final TagKey<Item> TWO_HANDED_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("two_handed_items"));
//    public static final TagKey<Item> NON_TWO_HANDED_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("non_two_handed_items"));
//    public static final TagKey<Item> MAIN_HAND_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("main_hand_items"));
//    public static final TagKey<Item> OFF_HAND_ITEMS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("off_hand_items"));
//    public static final TagKey<Item> EMPTY_HAND_WEAPONS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("empty_hand_weapons"));
//    public static final TagKey<Item> HELMETS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("helmets"));
//    public static final TagKey<Item> SHOULDERS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("shoulders"));
//    public static final TagKey<Item> CHEST_PLATES = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("chest_plates"));
//    public static final TagKey<Item> BELTS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("belts"));
//    public static final TagKey<Item> LEGGINGS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("leggings"));
//    public static final TagKey<Item> NECKLACES = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("necklaces"));
//    public static final TagKey<Item> RINGS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("rings"));
//    public static final TagKey<Item> GLOVES = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("gloves"));
//    public static final TagKey<Item> BOOTS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("boots"));
//    public static final TagKey<Item> SPELLS = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("spells"));
//
//    // enables functionality
//    public static final TagKey<Item> ENABLES_MANA_REGENERATION = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("enables_mana_regeneration"));
//    public static final TagKey<Item> IGNORES_ATTACK_MOVEMENT_PENALTY = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("ignores_attack_movement_penalty"));
//    public static final TagKey<Item> KEEPS_INVENTORY_ON_DEATH = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("keeps_inventory_on_death"));
//    public static final TagKey<Item> DOUBLES_INCOMING_DAMAGE = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("doubles_incoming_damage"));
//
//    // granting enchantments
//    public static final TagKey<Item> GRANTS_DEPTH_STRIDER_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_depth_strider_level_3"));
//    public static final TagKey<Item> GRANTS_LOOTING_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_looting_level_3"));
//    public static final TagKey<Item> GRANTS_UNBREAKING_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_unbreaking_level_3"));
//    public static final TagKey<Item> GRANTS_FEATHER_FALLING_LEVEL_4 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_feather_falling_level_4"));
//    public static final TagKey<Item> PREVENTS_NON_LETHAL_FALL_DAMAGE = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("prevents_non_lethal_fall_damage"));
//    public static final TagKey<Item> GRANTS_SWIFT_SNEAK_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_swift_sneak_level_3"));
//    public static final TagKey<Item> GRANTS_RESPIRATION_LEVEL_3 = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("grants_respiration_level_3"));
//
//    // special necklaces
//    public static final TagKey<Item> TELEPORT_HOME_NECKLACES = TagKey.of(RegistryKeys.ITEM, ScriptBlocksMod.identifier("teleport_home_necklaces"));
    //endregion ItemTags
}
