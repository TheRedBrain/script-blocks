package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.effect.ScriptBlocksStatusEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class ScriptBlocksStatusEffects {

	public static Holder<MobEffect> HOUSING_OWNER_EFFECT;
	public static Holder<MobEffect> HOUSING_CO_OWNER_EFFECT;
	public static Holder<MobEffect> HOUSING_TRUSTED_EFFECT;
	public static Holder<MobEffect> HOUSING_GUEST_EFFECT;
	public static Holder<MobEffect> HOUSING_STRANGER_EFFECT;
	public static Holder<MobEffect> BUILDING_MODE;
	public static Holder<MobEffect> ADVENTURE_EFFECT;

	public static final MobEffect HOUSING_OWNER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final MobEffect HOUSING_CO_OWNER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final MobEffect HOUSING_TRUSTED_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final MobEffect HOUSING_GUEST_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();
	public static final MobEffect HOUSING_STRANGER_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();

	public static final MobEffect BUILDING_MODE_INSTANCE = new ScriptBlocksStatusEffect();
	public static final MobEffect ADVENTURE_EFFECT_INSTANCE = new ScriptBlocksStatusEffect();

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
		ADVENTURE_EFFECT = register("adventure", ADVENTURE_EFFECT_INSTANCE);
	}

	private static Holder<MobEffect> register(String identifierString, MobEffect statusEffect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, ScriptBlocks.identifier(identifierString), statusEffect);
	}
}
