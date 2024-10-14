package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Dialogue(
		List<String> dialogueTextList,
		List<Identifier> answerList,
		@Nullable Identifier unlockAdvancement,
		@Nullable Identifier lockAdvancement,
		boolean cancellable
) {

	public static final Codec<Dialogue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().optionalFieldOf("dialogueTextList", List.of()).forGetter(x -> x.dialogueTextList),
			Identifier.CODEC.listOf().optionalFieldOf("answerList", List.of()).forGetter(x -> x.answerList),
			Identifier.CODEC.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
			Identifier.CODEC.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
			Codec.BOOL.optionalFieldOf("cancellable", true).forGetter(x -> x.cancellable)
			).apply(instance, Dialogue::new));

	public Dialogue(
			List<String> dialogueTextList,
			List<Identifier> answerList,
			@Nullable Identifier unlockAdvancement,
			@Nullable Identifier lockAdvancement,
			boolean cancellable
	) {
		this.dialogueTextList = dialogueTextList != null ? dialogueTextList : List.of();
		this.answerList = answerList != null ? answerList : List.of();
		this.unlockAdvancement = unlockAdvancement;
		this.lockAdvancement = lockAdvancement;
		this.cancellable = cancellable;
	}

}
