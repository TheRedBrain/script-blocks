package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ScriptBlocksDamageTypes {

	public static final ResourceKey<DamageType> PVP_WINNING_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ScriptBlocks.identifier("pvp_winning_damage_type"));
}
