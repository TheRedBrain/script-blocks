package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.ShopsRegistry;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class TradeWithShopPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TradeWithShopPacket> {
	@Override
	public void receive(TradeWithShopPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayerEntity serverPlayerEntity = context.player();

		String shopIdentifier = payload.shopIdentifier();
		int id = payload.id();

		ScreenHandler screenHandler = serverPlayerEntity.currentScreenHandler;
		List<Shop.Deal> dealsList = new ArrayList<>(List.of());
		Shop shop = null;
		if (!shopIdentifier.equals("")) {
			shop = ShopsRegistry.registeredShops.get(Identifier.of(shopIdentifier));
		}
		if (shop != null) {
			dealsList = shop.dealList();
		}

		Shop.Deal currentDeal = dealsList.get(id);
		if (currentDeal != null && screenHandler instanceof ShopScreenHandler shopScreenHandler) {
			boolean bl = true;
			for (ItemStack price : currentDeal.price()) {
				Item virtualItem = price.getItem();
				int priceCount = price.getCount();
				for (int j = 0; j < shopScreenHandler.inventory.size(); j++) {
					if (shopScreenHandler.inventory.getStack(j).isOf(virtualItem)) {
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
				serverPlayerEntity.getInventory().offerOrDrop(currentDeal.offer());
			}
		}
	}
}