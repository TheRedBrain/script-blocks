package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record DialogueAnswer(
		String answerText,
		@Nullable Identifier responseDialogue,
		@Nullable Identifier lockAdvancement,
		@Nullable Identifier unlockAdvancement,
		boolean showLockedAnswer,
		boolean showUnaffordableAnswer,
		@Nullable Identifier grantedAdvancement,
		@Nullable Identifier criterionName,
		@Nullable Identifier lootTable,
		String usedBlock,
		String triggeredBlock,
		String overlayMessage,
		List<ItemStack> itemCost
) {

	public static final Codec<DialogueAnswer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("answerText", "").forGetter(x -> x.answerText),
			Identifier.CODEC.optionalFieldOf("responseDialogue", null).forGetter(x -> x.responseDialogue),
			Identifier.CODEC.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
			Identifier.CODEC.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockedAnswer", true).forGetter(x -> x.showLockedAnswer),
			Codec.BOOL.optionalFieldOf("showUnaffordableAnswer", true).forGetter(x -> x.showUnaffordableAnswer),
			Identifier.CODEC.optionalFieldOf("grantedAdvancement", null).forGetter(x -> x.grantedAdvancement),
			Identifier.CODEC.optionalFieldOf("criterionName", null).forGetter(x -> x.criterionName),
			Identifier.CODEC.optionalFieldOf("lootTable", null).forGetter(x -> x.lootTable),
			Codec.STRING.optionalFieldOf("usedBlock", "").forGetter(x -> x.usedBlock),
			Codec.STRING.optionalFieldOf("triggeredBlock", "").forGetter(x -> x.triggeredBlock),
			Codec.STRING.optionalFieldOf("overlayMessage", "").forGetter(x -> x.overlayMessage),
			ItemStack.CODEC.listOf().optionalFieldOf("itemCost", List.of()).forGetter(x -> x.itemCost)
	).apply(instance, DialogueAnswer::new));

	public DialogueAnswer(
			String answerText,
			@Nullable Identifier responseDialogue,
			@Nullable Identifier lockAdvancement,
			@Nullable Identifier unlockAdvancement,
			boolean showLockedAnswer,
			boolean showUnaffordableAnswer,
			@Nullable Identifier grantedAdvancement,
			@Nullable Identifier criterionName,
			@Nullable Identifier lootTable,
			String usedBlock,
			String triggeredBlock,
			String overlayMessage,
			List<ItemStack> itemCost
	) {
		this.answerText = answerText != null ? answerText : "";
		this.responseDialogue = responseDialogue;
		this.lockAdvancement = lockAdvancement;
		this.unlockAdvancement = unlockAdvancement;
		this.showLockedAnswer = showLockedAnswer;
		this.showUnaffordableAnswer = showUnaffordableAnswer;
		this.grantedAdvancement = grantedAdvancement;
		this.criterionName = criterionName;
		this.lootTable = lootTable;
		this.usedBlock = usedBlock != null ? usedBlock : "";
		this.triggeredBlock = triggeredBlock != null ? triggeredBlock : "";
		this.overlayMessage = overlayMessage != null ? overlayMessage : "";
		this.itemCost = itemCost != null ? itemCost : List.of();
	}

}
