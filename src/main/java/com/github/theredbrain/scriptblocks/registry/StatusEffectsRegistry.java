package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.effect.NeutralStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class StatusEffectsRegistry {

	//region housing effects
	public static final StatusEffect HOUSING_OWNER_EFFECT = new NeutralStatusEffect();
	public static final StatusEffect HOUSING_CO_OWNER_EFFECT = new NeutralStatusEffect();
	public static final StatusEffect HOUSING_TRUSTED_EFFECT = new NeutralStatusEffect();
	public static final StatusEffect HOUSING_GUEST_EFFECT = new NeutralStatusEffect();
	public static final StatusEffect HOUSING_STRANGER_EFFECT = new NeutralStatusEffect();
	public static final StatusEffect EDIT_HOUSING_RESISTANCE_EFFECT = new NeutralStatusEffect();
	//endregion housing effects

	public static final StatusEffect BUILDING_MODE = new NeutralStatusEffect();
	public static final StatusEffect PORTAL_RESISTANCE_EFFECT = new NeutralStatusEffect();

	public static void registerEffects() {
		// housing effects
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("housing_owner_effect"), HOUSING_OWNER_EFFECT);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("housing_co_owner_effect"), HOUSING_CO_OWNER_EFFECT);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("housing_trusted_effect"), HOUSING_TRUSTED_EFFECT);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("housing_guest_effect"), HOUSING_GUEST_EFFECT);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("housing_stranger_effect"), HOUSING_STRANGER_EFFECT);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("edit_housing_resistance_effect"), EDIT_HOUSING_RESISTANCE_EFFECT);
		// utility effects
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("building_mode"), BUILDING_MODE);
		Registry.registerReference(Registries.STATUS_EFFECT, ScriptBlocks.identifier("portal_resistance_effect"), PORTAL_RESISTANCE_EFFECT);
	}
}
