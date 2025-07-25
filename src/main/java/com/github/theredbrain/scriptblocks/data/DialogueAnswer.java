package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.List;

public record DialogueAnswer(
		String answerText,
		List<String> responseDialogues,
		Availability availability,
		Results results
) {

	public static final Codec<DialogueAnswer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("answerText", "").forGetter(x -> x.answerText),
			Codec.STRING.listOf().optionalFieldOf("responseDialogues", null).forGetter(x -> x.responseDialogues),
			Availability.CODEC.fieldOf("availability").forGetter(x -> x.availability),
			Results.CODEC.fieldOf("results").forGetter(x -> x.results)
	).apply(instance, DialogueAnswer::new));

	public DialogueAnswer(
			String answerText,
			List<String> responseDialogues,
			Availability availability,
			Results results
	) {
		this.answerText = answerText != null ? answerText : "";
		this.responseDialogues = responseDialogues != null ? responseDialogues : List.of();
		this.availability = availability;
		this.results = results;
	}

	public record Availability(
			String unlockAdvancement,
			String lockAdvancement,
			DataCheck unlockDataCheck,
			DataCheck lockDataCheck,
			boolean showLockedAnswer,
			List<ItemCost> itemCosts,
			boolean showUnaffordableAnswer
	) {

		public static final Codec<Availability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.STRING.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
				DataCheck.CODEC.optionalFieldOf("unlockDataCheck", DataCheck.DEFAULT).forGetter(x -> x.unlockDataCheck),
				DataCheck.CODEC.optionalFieldOf("lockDataCheck", DataCheck.DEFAULT).forGetter(x -> x.lockDataCheck),
				Codec.BOOL.optionalFieldOf("showLockedAnswer", true).forGetter(x -> x.showLockedAnswer),
				ItemCost.CODEC.listOf().optionalFieldOf("itemCosts", List.of()).forGetter(x -> x.itemCosts),
				Codec.BOOL.optionalFieldOf("showUnaffordableAnswer", true).forGetter(x -> x.showUnaffordableAnswer)
		).apply(instance, Availability::new));

		public Availability(
				String unlockAdvancement,
				String lockAdvancement,
				DataCheck unlockDataCheck,
				DataCheck lockDataCheck,
				boolean showLockedAnswer,
				List<ItemCost> itemCosts,
				boolean showUnaffordableAnswer
		) {
			this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
			this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
			this.unlockDataCheck = unlockDataCheck != null ? unlockDataCheck : DataCheck.DEFAULT;
			this.lockDataCheck = lockDataCheck != null ? lockDataCheck : DataCheck.DEFAULT;
			this.showLockedAnswer = showLockedAnswer;
			this.itemCosts = itemCosts != null ? itemCosts : List.of();
			this.showUnaffordableAnswer = showUnaffordableAnswer;
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
	}

	public record Results(
			String grantedAdvancement,
			String criterionName,
			String lootTable,
			String usedBlock,
			String triggeredBlock,
			String overlayMessage
	) {

		public static final Codec<Results> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("grantedAdvancement", null).forGetter(x -> x.grantedAdvancement),
				Codec.STRING.optionalFieldOf("criterionName", null).forGetter(x -> x.criterionName),
				Codec.STRING.optionalFieldOf("lootTable", null).forGetter(x -> x.lootTable),
				Codec.STRING.optionalFieldOf("usedBlock", "").forGetter(x -> x.usedBlock),
				Codec.STRING.optionalFieldOf("triggeredBlock", "").forGetter(x -> x.triggeredBlock),
				Codec.STRING.optionalFieldOf("overlayMessage", "").forGetter(x -> x.overlayMessage)
		).apply(instance, Results::new));

		public Results(
				String grantedAdvancement,
				String criterionName,
				String lootTable,
				String usedBlock,
				String triggeredBlock,
				String overlayMessage
		) {
			this.grantedAdvancement = grantedAdvancement != null ? grantedAdvancement : "";
			this.criterionName = criterionName != null ? criterionName : "";
			this.lootTable = lootTable != null ? lootTable : "";
			this.usedBlock = usedBlock != null ? usedBlock : "";
			this.triggeredBlock = triggeredBlock != null ? triggeredBlock : "";
			this.overlayMessage = overlayMessage != null ? overlayMessage : "";
		}
	}
}
