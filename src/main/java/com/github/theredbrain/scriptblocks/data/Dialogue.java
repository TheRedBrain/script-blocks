package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.List;

public record Dialogue(
		List<String> dialogueTextList,
		List<Identifier> answerList,
		String unlockAdvancement,
		String lockAdvancement,
		boolean cancellable
) {

	public static final Codec<Dialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().optionalFieldOf("dialogueTextList", List.of()).forGetter(x -> x.dialogueTextList),
			Identifier.CODEC.listOf().optionalFieldOf("answerList", List.of()).forGetter(x -> x.answerList),
			Codec.STRING.optionalFieldOf("unlockAdvancement", "").forGetter(x -> x.unlockAdvancement),
			Codec.STRING.optionalFieldOf("lockAdvancement", "").forGetter(x -> x.lockAdvancement),
			Codec.BOOL.optionalFieldOf("cancellable", true).forGetter(x -> x.cancellable)
			).apply(instance, Dialogue::new));

	public Dialogue(
			List<String> dialogueTextList,
			List<Identifier> answerList,
			String unlockAdvancement,
			String lockAdvancement,
			boolean cancellable
	) {
		this.dialogueTextList = dialogueTextList != null ? dialogueTextList : List.of();
		this.answerList = answerList != null ? answerList : List.of();
		this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
		this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
		this.cancellable = cancellable;
	}

}
