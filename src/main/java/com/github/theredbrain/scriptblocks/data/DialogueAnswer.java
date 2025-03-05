package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record DialogueAnswer(
		String answerText,
		List<String> responseDialogues,
		String unlockAdvancement,
		String lockAdvancement,
		boolean showLockedAnswer,
		boolean showUnaffordableAnswer,
		String grantedAdvancement,
		String criterionName,
		String lootTable,
		String usedBlock,
		String triggeredBlock,
		String overlayMessage,
		boolean consumeItem,
		String itemIdentifier,
		int itemCount
) {

	public static final Codec<DialogueAnswer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("answerText", "").forGetter(x -> x.answerText),
			Codec.STRING.listOf().optionalFieldOf("responseDialogues", null).forGetter(x -> x.responseDialogues),
			Codec.STRING.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
			Codec.STRING.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockedAnswer", true).forGetter(x -> x.showLockedAnswer),
			Codec.BOOL.optionalFieldOf("showUnaffordableAnswer", true).forGetter(x -> x.showUnaffordableAnswer),
			Codec.STRING.optionalFieldOf("grantedAdvancement", null).forGetter(x -> x.grantedAdvancement),
			Codec.STRING.optionalFieldOf("criterionName", null).forGetter(x -> x.criterionName),
			Codec.STRING.optionalFieldOf("lootTable", null).forGetter(x -> x.lootTable),
			Codec.STRING.optionalFieldOf("usedBlock", "").forGetter(x -> x.usedBlock),
			Codec.STRING.optionalFieldOf("triggeredBlock", "").forGetter(x -> x.triggeredBlock),
			Codec.STRING.optionalFieldOf("overlayMessage", "").forGetter(x -> x.overlayMessage),
			Codec.BOOL.optionalFieldOf("consumeItem", true).forGetter(x -> x.consumeItem),
			Codec.STRING.optionalFieldOf("itemIdentifier", "").forGetter(x -> x.itemIdentifier),
			Codec.INT.optionalFieldOf("itemCount", 0).forGetter(x -> x.itemCount)
	).apply(instance, DialogueAnswer::new));

	public DialogueAnswer(
			String answerText,
			List<String> responseDialogues,
			String unlockAdvancement,
			String lockAdvancement,
			boolean showLockedAnswer,
			boolean showUnaffordableAnswer,
			String grantedAdvancement,
			String criterionName,
			String lootTable,
			String usedBlock,
			String triggeredBlock,
			String overlayMessage,
			boolean consumeItem,
			String itemIdentifier,
			int itemCount
	) {
		this.answerText = answerText != null ? answerText : "";
		this.responseDialogues = responseDialogues != null ? responseDialogues : List.of();
		this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
		this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
		this.showLockedAnswer = showLockedAnswer;
		this.showUnaffordableAnswer = showUnaffordableAnswer;
		this.grantedAdvancement = grantedAdvancement != null ? grantedAdvancement : "";
		this.criterionName = criterionName != null ? criterionName : "";
		this.lootTable = lootTable != null ? lootTable : "";
		this.usedBlock = usedBlock != null ? usedBlock : "";
		this.triggeredBlock = triggeredBlock != null ? triggeredBlock : "";
		this.overlayMessage = overlayMessage != null ? overlayMessage : "";
		this.consumeItem = consumeItem;
		this.itemIdentifier = itemIdentifier != null ? itemIdentifier : "";
		this.itemCount = itemCount;
	}

}
