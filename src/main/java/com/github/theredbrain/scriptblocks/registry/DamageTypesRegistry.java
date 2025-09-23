package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class DamageTypesRegistry {

	public static final RegistryKey<DamageType> PVP_WINNING_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, ScriptBlocks.identifier("pvp_winning_damage_type"));
}
