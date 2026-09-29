package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.effect.ScriptBlocksStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ScriptBlocksStatusEffects {

	public static RegistryEntry<StatusEffect> HOUSING_OWNER_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_CO_OWNER_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_TRUSTED_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_GUEST_EFFECT;
	public static RegistryEntry<StatusEffect> HOUSING_STRANGER_EFFECT;
	public static RegistryEntry<StatusEffect> BUILDING_MODE;
	public static RegistryEntry<StatusEffect> PORTAL_RESISTANCE_EFFECT;
	public static RegistryEntry<StatusEffect> ADVENTURE_EFFECT;

	public static final StatusEffect HOUSING_OWNER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_CO_OWNER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_TRUSTED_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_GUEST_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect HOUSING_STRANGER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();

	public static final StatusEffect BUILDING_MODE_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect PORTAL_RESISTANCE_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final StatusEffect ADVENTURE_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();

	public static void bootstrap() {
	}

	static {
		// --- Registration ---
		HOUSING_OWNER_EFFECT = register("housing_owner_effect", HOUSING_OWNER_EFFECT_INSTANCE);
		HOUSING_CO_OWNER_EFFECT = register("housing_co_owner_effect", HOUSING_CO_OWNER_EFFECT_INSTANCE);
		HOUSING_TRUSTED_EFFECT = register("housing_trusted_effect", HOUSING_TRUSTED_EFFECT_INSTANCE);
		HOUSING_GUEST_EFFECT = register("housing_guest_effect", HOUSING_GUEST_EFFECT_INSTANCE);
		HOUSING_STRANGER_EFFECT = register("housing_stranger_effect", HOUSING_STRANGER_EFFECT_INSTANCE);
		BUILDING_MODE = register("building_mode", BUILDING_MODE_INSTANCE);
		PORTAL_RESISTANCE_EFFECT = register("portal_resistance_effect", PORTAL_RESISTANCE_EFFECT_INSTANCE);
		ADVENTURE_EFFECT = register("adventure", ADVENTURE_EFFECT_INSTANCE);
	}

	private static RegistryEntry<StatusEffect> register(String identifierString, StatusEffect statusEffect) {
		return Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier(identifierString), statusEffect);
	}
}
