package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ShopHelper {

	@Nullable
	public static Shop getShop(RegistryAccess registryAccess, Identifier identifier) {
		Shop shop = null;
		Optional<Holder.Reference<Registry<Shop>>> optionalShopRegistryReference = registryAccess.get(ScriptBlocksDynamicRegistries.SHOP_REGISTRY_KEY);
		if (optionalShopRegistryReference.isPresent()) {
			Optional<Holder.Reference<Shop>> optionalShopReference = optionalShopRegistryReference.get().value().get(identifier);
			if (optionalShopReference.isPresent()) {
				shop = optionalShopReference.get().value();
			}
		}
		return shop;
	}
}
