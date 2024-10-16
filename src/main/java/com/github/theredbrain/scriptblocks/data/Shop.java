package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

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
			ItemStack offer,
			List<ItemStack> price,
			int maxStockCount,
			@Nullable Identifier lockAdvancement,
			@Nullable Identifier unlockAdvancement,
			boolean showLockedDeal
	) {

		public static final Codec<Deal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.CODEC.optionalFieldOf("offer", ItemStack.EMPTY).forGetter(x -> x.offer),
				ItemStack.CODEC.listOf().optionalFieldOf("price", List.of()).forGetter(x -> x.price),
				Codec.INT.optionalFieldOf("maxStockCount", 1).forGetter(x -> x.maxStockCount),
				Identifier.CODEC.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
				Identifier.CODEC.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockedDeal", true).forGetter(x -> x.showLockedDeal)
		).apply(instance, Deal::new));

		public Deal(
				ItemStack offer,
				List<ItemStack> price,
				int maxStockCount,
				@Nullable Identifier lockAdvancement,
				@Nullable Identifier unlockAdvancement,
				boolean showLockedDeal
		) {
			this.price = price != null ? price : List.of();
			this.offer = offer;
			this.maxStockCount = maxStockCount;
			this.lockAdvancement = lockAdvancement;
			this.unlockAdvancement = unlockAdvancement;
			this.showLockedDeal = showLockedDeal;
		}

	}
}
