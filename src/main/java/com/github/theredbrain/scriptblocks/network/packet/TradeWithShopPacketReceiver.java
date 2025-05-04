package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.CustomDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeWithShopPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TradeWithShopPacket> {
	@Override
	public void receive(TradeWithShopPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		String shopIdentifier = payload.shopIdentifier();
		int id = payload.id();

		ScreenHandler screenHandler = serverPlayerEntity.currentScreenHandler;
		List<Shop.Deal> dealsList = new ArrayList<>(List.of());
		Shop shop = null;
		if (!shopIdentifier.isEmpty()) {
			Optional<RegistryEntry.Reference<Shop>> optionalShopReference = serverPlayerEntity.getWorld().getRegistryManager().get(CustomDynamicRegistries.SHOP_REGISTRY_KEY).getEntry(Identifier.of(shopIdentifier));
			if (optionalShopReference.isPresent()) {
				shop = optionalShopReference.get().value();
			}
		}
		if (shop != null) {
			dealsList = shop.dealList();
		}

		Shop.Deal currentDeal = dealsList.get(id);
		if (currentDeal != null && screenHandler instanceof ShopScreenHandler shopScreenHandler) {
			boolean bl = true;
			for (Shop.Deal.Item price : currentDeal.price()) {
				Item priceItem = Registries.ITEM.get(Identifier.tryParse(price.id()));
				int priceCount = price.count();
				for (int j = 0; j < shopScreenHandler.inventory.size(); j++) {
					if (shopScreenHandler.inventory.getStack(j).isOf(priceItem)) {
						ItemStack itemStack = shopScreenHandler.slots.get(j + 36).getStack().copy();
						int stackCount = itemStack.getCount();
						if (stackCount >= priceCount) {
							itemStack.setCount(stackCount - priceCount);
							shopScreenHandler.slots.get(j + 36).setStack(itemStack);
							priceCount = 0;
							break;
						} else {
							shopScreenHandler.slots.get(j + 36).setStack(ItemStack.EMPTY);
							priceCount = priceCount - stackCount;
						}
					}
				}
				if (priceCount > 0) {
					bl = false;
				}
			}
			if (bl) {
				for (int j = 0; j < currentDeal.offer().size(); j++) {
					Shop.Deal.Item virtualItem = currentDeal.offer().get(j);
					ItemStack itemStack = Registries.ITEM.get(Identifier.tryParse(virtualItem.id())).getDefaultStack();
					itemStack.setCount(virtualItem.count());
					serverPlayerEntity.getInventory().offerOrDrop(itemStack);
				}
			}
		}
	}
}