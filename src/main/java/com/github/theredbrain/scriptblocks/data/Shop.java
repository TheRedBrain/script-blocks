package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

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
			Codec.STRING.optionalFieldOf("shopTitle", "").forGetter(x -> x.shopTitle),
			Codec.STRING.optionalFieldOf("offersTitle", "").forGetter(x -> x.offersTitle),
			Deal.CODEC.listOf().optionalFieldOf("dealList", List.of()).forGetter(x -> x.dealList)
	).apply(instance, Shop::new));

	public record Deal(
			List<Item> offer,
			List<Item> price,
			int maxStockCount,
			String unlockAdvancement,
			String lockAdvancement,
			boolean showLockedDeal
	) {

		public static final Codec<Deal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Item.CODEC.listOf().optionalFieldOf("offer", List.of()).forGetter(x -> x.offer),
				Item.CODEC.listOf().optionalFieldOf("price", List.of()).forGetter(x -> x.price),
				Codec.INT.optionalFieldOf("maxStockCount", 1).forGetter(x -> x.maxStockCount),
				Codec.STRING.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.STRING.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockedDeal", true).forGetter(x -> x.showLockedDeal)
		).apply(instance, Deal::new));

		public Deal(
				List<Item> offer,
				List<Item> price,
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

		public record Item(
				String id,
				int count
		) {

			public static final Codec<Shop.Deal.Item> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.optionalFieldOf("id", "").forGetter(x -> x.id),
					Codec.INT.optionalFieldOf("count", 0).forGetter(x -> x.count)
			).apply(instance, Shop.Deal.Item::new));

			public Item(
					String id,
					int count
			) {
				this.id = id !=  null ? id : "";
				this.count = count;
			}

		}
	}
}
