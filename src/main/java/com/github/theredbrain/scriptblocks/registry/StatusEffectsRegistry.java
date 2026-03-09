package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.effect.ScriptBlocksStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class StatusEffectsRegistry {

	public static final StatusEffect HOUSING_OWNER_EFFECT = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_CO_OWNER_EFFECT = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_TRUSTED_EFFECT = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_GUEST_EFFECT = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_STRANGER_EFFECT = new ScriptBlocksStatusEffect();
//	public static final StatusEffect EDIT_HOUSING_RESISTANCE_EFFECT = new ScriptBlocksStatusEffect();

	public static final StatusEffect BUILDING_MODE = new ScriptBlocksStatusEffect();
	public static final StatusEffect PORTAL_RESISTANCE_EFFECT = new ScriptBlocksStatusEffect();

	public static void registerEffects() {
		// --- Registration ---
		ScriptBlocks.HOUSING_OWNER_EFFECT = register("housing_owner_effect", HOUSING_OWNER_EFFECT);
		ScriptBlocks.HOUSING_CO_OWNER_EFFECT = register("housing_co_owner_effect", HOUSING_CO_OWNER_EFFECT);
		ScriptBlocks.HOUSING_TRUSTED_EFFECT = register("housing_trusted_effect", HOUSING_TRUSTED_EFFECT);
		ScriptBlocks.HOUSING_GUEST_EFFECT = register("housing_guest_effect", HOUSING_GUEST_EFFECT);
		ScriptBlocks.HOUSING_STRANGER_EFFECT = register("housing_stranger_effect", HOUSING_STRANGER_EFFECT);
//		ScriptBlocks.EDIT_HOUSING_RESISTANCE_EFFECT = register("edit_housing_resistance_effect", EDIT_HOUSING_RESISTANCE_EFFECT);
		ScriptBlocks.BUILDING_MODE = register("building_mode", BUILDING_MODE);
		ScriptBlocks.PORTAL_RESISTANCE_EFFECT = register("portal_resistance_effect", PORTAL_RESISTANCE_EFFECT);
	}

	private static RegistryEntry<StatusEffect> register(String identifierString, StatusEffect statusEffect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier(identifierString), statusEffect);
	}
}
