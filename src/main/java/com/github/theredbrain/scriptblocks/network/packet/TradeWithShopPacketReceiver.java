package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.data.Shop;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import com.github.theredbrain.scriptblocks.screen.ShopScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeWithShopPacketReceiver implements ServerPlayNetworking.PlayPayloadHandler<TradeWithShopPacket> {
	@Override
	public void receive(TradeWithShopPacket payload, ServerPlayNetworking.Context context) {

		ServerPlayer serverPlayerEntity = context.player();

		String shopIdentifier = payload.shopIdentifier();
		int id = payload.id();

		AbstractContainerMenu screenHandler = serverPlayerEntity.containerMenu;
		List<Shop.Deal> dealsList = new ArrayList<>(List.of());
		Shop shop = null;
		if (!shopIdentifier.isEmpty()) {
			Optional<Holder.Reference<Shop>> optionalShopReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.SHOP_REGISTRY_KEY).getEntry(Identifier.parse(shopIdentifier));
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
			for (ItemStack priceStack : currentDeal.price()) {
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
					ItemStack offerStack = currentDeal.offer().get(j);
					serverPlayerEntity.getInventory().placeItemBackInInventory(offerStack);
				}
			}
		}
	}
}