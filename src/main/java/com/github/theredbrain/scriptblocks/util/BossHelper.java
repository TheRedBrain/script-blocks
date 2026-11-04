package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class BossHelper {

	@Nullable
	public static Boss getBoss(RegistryAccess registryAccess, Identifier identifier) {
		Boss boss = null;
		Optional<Holder.Reference<Registry<Boss>>> optionalBossRegistryReference = registryAccess.get(ScriptBlocksDynamicRegistries.BOSS_REGISTRY_KEY);
		if (optionalBossRegistryReference.isPresent()) {
			Optional<Holder.Reference<Boss>> optionalBossReference = optionalBossRegistryReference.get().value().get(identifier);
			if (optionalBossReference.isPresent()) {
				boss = optionalBossReference.get().value();
			}
		}
		return boss;
	}
}
