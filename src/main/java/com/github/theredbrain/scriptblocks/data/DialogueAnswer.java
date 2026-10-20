package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DialogueAnswer(
		String answerText,
		List<String> responseDialogues,
		Availability availability,
		Results results
) {

	public static final Codec<DialogueAnswer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("answer_text", "").forGetter(x -> x.answerText),
			Codec.STRING.listOf().optionalFieldOf("response_dialogues", null).forGetter(x -> x.responseDialogues),
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
			CommonDataStructures.DataCheck unlockDataCheck,
			CommonDataStructures.DataCheck lockDataCheck,
			boolean showLockedAnswer,
			List<CommonDataStructures.ItemCost> itemCosts,
			boolean showUnaffordableAnswer
	) {

		public static final Codec<Availability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("unlock_advancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.STRING.optionalFieldOf("lock_advancement", null).forGetter(x -> x.lockAdvancement),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("unlock_data_check", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.unlockDataCheck),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("lock_data_check", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.lockDataCheck),
				Codec.BOOL.optionalFieldOf("show_locked_answer", true).forGetter(x -> x.showLockedAnswer),
				CommonDataStructures.ItemCost.CODEC.listOf().optionalFieldOf("item_costs", List.of()).forGetter(x -> x.itemCosts),
				Codec.BOOL.optionalFieldOf("show_unaffordable_answer", true).forGetter(x -> x.showUnaffordableAnswer)
		).apply(instance, Availability::new));

		public Availability(
				String unlockAdvancement,
				String lockAdvancement,
				CommonDataStructures.DataCheck unlockDataCheck,
				CommonDataStructures.DataCheck lockDataCheck,
				boolean showLockedAnswer,
				List<CommonDataStructures.ItemCost> itemCosts,
				boolean showUnaffordableAnswer
		) {
			this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
			this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
			this.unlockDataCheck = unlockDataCheck != null ? unlockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.lockDataCheck = lockDataCheck != null ? lockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.showLockedAnswer = showLockedAnswer;
			this.itemCosts = itemCosts != null ? itemCosts : List.of();
			this.showUnaffordableAnswer = showUnaffordableAnswer;
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
				Codec.STRING.optionalFieldOf("granted_advancement", null).forGetter(x -> x.grantedAdvancement),
				Codec.STRING.optionalFieldOf("criterion_name", null).forGetter(x -> x.criterionName),
				Codec.STRING.optionalFieldOf("loot_table", null).forGetter(x -> x.lootTable),
				Codec.STRING.optionalFieldOf("used_block", "").forGetter(x -> x.usedBlock),
				Codec.STRING.optionalFieldOf("triggered_block", "").forGetter(x -> x.triggeredBlock),
				Codec.STRING.optionalFieldOf("overlay_message", "").forGetter(x -> x.overlayMessage)
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
