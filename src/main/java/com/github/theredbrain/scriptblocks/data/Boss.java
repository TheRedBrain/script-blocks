package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Boss(
		String bossEntityTypeId,
		boolean discardEntityAtEnd,
		List<Phase> phases
) {

	public static final Codec<Boss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("bossEntityTypeId", "").forGetter(x -> x.bossEntityTypeId),
			Codec.BOOL.optionalFieldOf("discardEntityAtEnd", true).forGetter(x -> x.discardEntityAtEnd),
			Phase.CODEC.listOf().optionalFieldOf("phases", List.of()).forGetter(x -> x.phases)
	).apply(instance, Boss::new));

	public Boss(
			String bossEntityTypeId,
			boolean discardEntityAtEnd,
			List<Phase> phases
	) {
		this.bossEntityTypeId = bossEntityTypeId != null ? bossEntityTypeId : "";
		this.discardEntityAtEnd = discardEntityAtEnd;
		this.phases = phases;
	}

	public record Phase(
			int bossHealthThreshold,
			int globalTimerThreshold,
			int phaseTimerThreshold,
			boolean triggerEndsPhase,
			float newHealthRatio,
			List<AttributeModifierEntry> entityAttributeModifiers,
			@Nullable String triggeredBlockAtStart,
			@Nullable String triggeredBlockAtEnd
	) {

		public static final Codec<Phase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.optionalFieldOf("bossHealthThreshold", 0).forGetter(x -> x.bossHealthThreshold),
				Codec.INT.optionalFieldOf("globalTimerThreshold", 0).forGetter(x -> x.globalTimerThreshold),
				Codec.INT.optionalFieldOf("phaseTimerThreshold", 0).forGetter(x -> x.phaseTimerThreshold),
				Codec.BOOL.optionalFieldOf("triggerEndsPhase", false).forGetter(x -> x.triggerEndsPhase),
				Codec.FLOAT.optionalFieldOf("newHealthRatio", 0.0F).forGetter(x -> x.newHealthRatio),
				AttributeModifierEntry.CODEC.listOf().optionalFieldOf("entityAttributeModifiers", List.of()).forGetter(x -> x.entityAttributeModifiers),
				Codec.STRING.optionalFieldOf("triggeredBlockAtStart", "").forGetter(x -> x.triggeredBlockAtStart),
				Codec.STRING.optionalFieldOf("triggeredBlockAtEnd", "").forGetter(x -> x.triggeredBlockAtEnd)
		).apply(instance, Phase::new));

		public Phase(
				int bossHealthThreshold,
				int globalTimerThreshold,
				int phaseTimerThreshold,
				boolean triggerEndsPhase,
				float newHealthRatio,
				List<AttributeModifierEntry> entityAttributeModifiers,
				@Nullable String triggeredBlockAtStart,
				@Nullable String triggeredBlockAtEnd
		) {
			this.bossHealthThreshold = bossHealthThreshold;
			this.globalTimerThreshold = globalTimerThreshold;
			this.phaseTimerThreshold = phaseTimerThreshold;
			this.triggerEndsPhase = triggerEndsPhase;
			this.newHealthRatio = newHealthRatio;
			this.entityAttributeModifiers = entityAttributeModifiers != null ? entityAttributeModifiers : List.of();
			this.triggeredBlockAtStart = triggeredBlockAtStart != null ? triggeredBlockAtStart : "";
			this.triggeredBlockAtEnd = triggeredBlockAtEnd != null ? triggeredBlockAtEnd : "";
		}

		public record AttributeModifierEntry(
				String attributeIdentifier,
				String id,
				double amount,
				String operation
		) {

			public static final Codec<Boss.Phase.AttributeModifierEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("attributeIdentifier").forGetter(x -> x.attributeIdentifier),
					Codec.STRING.fieldOf("id").forGetter(x -> x.id),
					Codec.DOUBLE.fieldOf("amount").forGetter(x -> x.amount),
					Codec.STRING.fieldOf("operation").forGetter(x -> x.operation)
			).apply(instance, Boss.Phase.AttributeModifierEntry::new));

			public AttributeModifierEntry(
					String attributeIdentifier,
					String id,
					double amount,
					String operation
			) {
				this.attributeIdentifier = attributeIdentifier;
				this.id = id;
				this.amount = amount;
				this.operation = operation;
			}
		}
	}

}
