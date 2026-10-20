package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record Dialogue(
		List<String> dialogueTextList,
		int dialogueTextsStartingIndex,
		List<String> answerList,
		int answersStartingIndex,
		String unlockAdvancement,
		String lockAdvancement,
		boolean cancellable
) {

	public static final Codec<Dialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().optionalFieldOf("dialogue_text_list", List.of()).forGetter(x -> x.dialogueTextList),
			Codec.INT.optionalFieldOf("dialogue_texts_starting_index", 0).forGetter(x -> x.dialogueTextsStartingIndex),
			Codec.STRING.listOf().optionalFieldOf("answer_list", List.of()).forGetter(x -> x.answerList),
			Codec.INT.optionalFieldOf("answers_starting_index", 7).forGetter(x -> x.answersStartingIndex),
			Codec.STRING.optionalFieldOf("unlock_advancement", null).forGetter(x -> x.unlockAdvancement),
			Codec.STRING.optionalFieldOf("lock_advancement", null).forGetter(x -> x.lockAdvancement),
			Codec.BOOL.optionalFieldOf("cancellable", true).forGetter(x -> x.cancellable)
	).apply(instance, Dialogue::new));

	public Dialogue(
			List<String> dialogueTextList,
			int dialogueTextsStartingIndex,
			List<String> answerList,
			int answersStartingIndex,
			String unlockAdvancement,
			String lockAdvancement,
			boolean cancellable
	) {
		this.dialogueTextList = dialogueTextList != null ? dialogueTextList : List.of();
		this.dialogueTextsStartingIndex = dialogueTextsStartingIndex;
		this.answerList = answerList != null ? answerList : List.of();
		this.answersStartingIndex = answersStartingIndex;
		this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
		this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
		this.cancellable = cancellable;
	}

}
