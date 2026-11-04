package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import com.github.theredbrain.scriptblocks.util.ShopHelper;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeWithShopPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TradeWithShopPacket> {
	@Override
	public void receive(TradeWithShopPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		String shopIdentifierString = payload.shopIdentifier();
		int id = payload.id();

		AbstractContainerMenu screenHandler = serverPlayerEntity.containerMenu;
		List<Shop.Deal> dealsList = new ArrayList<>(List.of());
		Shop shop = null;
		if (!shopIdentifierString.isEmpty()) {
			Identifier identifier = Identifier.tryParse(shopIdentifierString);
			if (identifier != null) {
				shop = ShopHelper.getShop(serverPlayerEntity.level().registryAccess(), identifier);
			}
		}
		if (shop != null) {
			dealsList = shop.dealList();
		}

		if (id > dealsList.size()) {
			return;
		}
		Shop.Deal currentDeal = dealsList.get(id);
		if (currentDeal != null && screenHandler instanceof ShopScreenHandler shopScreenHandler) {
			boolean bl = true;
			for (ItemStackTemplate priceStackTemplate : currentDeal.price()) {
				ItemStack priceStack = priceStackTemplate.create();
				int priceCount = priceStack.getCount();
				for (int j = 0; j < shopScreenHandler.inventory.getContainerSize(); j++) {
					if (ItemStack.isSameItemSameComponents(shopScreenHandler.inventory.getItem(j), priceStack)) {
						ItemStack itemStack = shopScreenHandler.slots.get(j + 36).getItem().copy();
						int stackCount = itemStack.getCount();
						if (stackCount >= priceCount) {
							itemStack.setCount(stackCount - priceCount);
							shopScreenHandler.slots.get(j + 36).setByPlayer(itemStack);
							priceCount = 0;
							break;
						} else {
							shopScreenHandler.slots.get(j + 36).setByPlayer(ItemStack.EMPTY);
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
					ItemStack offerStack = currentDeal.offer().get(j).create();
					serverPlayerEntity.getInventory().placeItemBackInInventory(offerStack);
				}
			}
		}
	}
}