package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

public class CommonDataStructures {

	public record DataCheck(
			String dataIdentifier,
			String dataValue,
			int comparisonMode
	) {

		public static final DataCheck DEFAULT = new DataCheck("", "", 0);
		public static final Codec<DataCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("dataIdentifier").forGetter(x -> x.dataIdentifier),
				Codec.STRING.fieldOf("dataValue").forGetter(x -> x.dataValue),
				Codec.INT.fieldOf("comparisonMode").forGetter(x -> x.comparisonMode)
		).apply(instance, DataCheck::new));

		public DataCheck(
				String dataIdentifier,
				String dataValue,
				int comparisonMode
		) {
			this.dataIdentifier = dataIdentifier;
			this.dataValue = dataValue;
			this.comparisonMode = comparisonMode;
		}
	}

	public record BlockPos(
			int x,
			int y,
			int z
	) {

	}

	public record ItemCost(
			ItemStack itemStack,
			boolean consumeStack
	) {

		public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ItemStack.VALIDATED_CODEC.fieldOf("itemStack").forGetter(x -> x.itemStack),
				Codec.BOOL.optionalFieldOf("consumeStack", true).forGetter(x -> x.consumeStack)
		).apply(instance, ItemCost::new));

		public ItemCost(
				ItemStack itemStack,
				boolean consumeStack
		) {
			this.itemStack = itemStack;
			this.consumeStack = consumeStack;
		}
	}
}
