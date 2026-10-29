package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

public record Shop(
		String shopTitle,
		String offersTitle,
		List<Deal> dealList
) {

	public Shop(
			String shopTitle,
			String offersTitle,
			List<Deal> dealList
	) {
		this.shopTitle = shopTitle;
		this.offersTitle = offersTitle;
		this.dealList = dealList != null ? dealList : List.of();
	}

	public static final Codec<Shop> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("shop_title", "").forGetter(x -> x.shopTitle),
			Codec.STRING.optionalFieldOf("offers_title", "").forGetter(x -> x.offersTitle),
			Deal.CODEC.listOf().optionalFieldOf("deal_list", List.of()).forGetter(x -> x.dealList)
	).apply(instance, Shop::new));

	public record Deal(
			List<ItemStack> offer,
			List<ItemStack> price,
			int maxStockCount,
			String unlockAdvancement,
			String lockAdvancement,
			boolean showLockedDeal
	) {

		public static final Codec<Deal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.CODEC.listOf().optionalFieldOf("offer", List.of()).forGetter(x -> x.offer),
				ItemStack.CODEC.listOf().optionalFieldOf("price", List.of()).forGetter(x -> x.price),
				Codec.INT.optionalFieldOf("max_stock_count", 1).forGetter(x -> x.maxStockCount),
				Codec.STRING.optionalFieldOf("unlock_advancement", "").forGetter(x -> x.unlockAdvancement),
				Codec.STRING.optionalFieldOf("lock_advancement", "").forGetter(x -> x.lockAdvancement),
				Codec.BOOL.optionalFieldOf("show_locked_deal", true).forGetter(x -> x.showLockedDeal)
		).apply(instance, Deal::new));

		public Deal(
				List<ItemStack> offer,
				List<ItemStack> price,
				int maxStockCount,
				String unlockAdvancement,
				String lockAdvancement,
				boolean showLockedDeal
		) {
			this.price = price != null ? price : List.of();
			this.offer = offer != null ? offer : List.of();
			this.maxStockCount = maxStockCount;
			this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
			this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
			this.showLockedDeal = showLockedDeal;
		}
	}
}
