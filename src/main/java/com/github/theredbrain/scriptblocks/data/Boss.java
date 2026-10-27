package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Boss(
		String bossEntityTypeId,
		boolean discardEntityAtEnd,
		boolean triggersBlockWhenNoPlayersAround,
		List<Phase> phases
) {

	public static final Codec<Boss> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("boss_entity_type_id", "").forGetter(x -> x.bossEntityTypeId),
			Codec.BOOL.optionalFieldOf("discard_entity_at_end", true).forGetter(x -> x.discardEntityAtEnd),
			Codec.BOOL.optionalFieldOf("triggers_block_when_no_players_around", false).forGetter(x -> x.triggersBlockWhenNoPlayersAround),
			Phase.CODEC.listOf().optionalFieldOf("phases", List.of()).forGetter(x -> x.phases)
	).apply(instance, Boss::new));

	public Boss(
			String bossEntityTypeId,
			boolean discardEntityAtEnd,
			boolean triggersBlockWhenNoPlayersAround,
			List<Phase> phases
	) {
		this.bossEntityTypeId = bossEntityTypeId != null ? bossEntityTypeId : "";
		this.discardEntityAtEnd = discardEntityAtEnd;
		this.triggersBlockWhenNoPlayersAround = triggersBlockWhenNoPlayersAround;
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
				Codec.INT.optionalFieldOf("boss_health_threshold", 0).forGetter(x -> x.bossHealthThreshold),
				Codec.INT.optionalFieldOf("global_timer_threshold", 0).forGetter(x -> x.globalTimerThreshold),
				Codec.INT.optionalFieldOf("phase_timer_threshold", 0).forGetter(x -> x.phaseTimerThreshold),
				Codec.BOOL.optionalFieldOf("trigger_ends_phase", false).forGetter(x -> x.triggerEndsPhase),
				Codec.FLOAT.optionalFieldOf("new_health_ratio", 0.0F).forGetter(x -> x.newHealthRatio),
				AttributeModifierEntry.CODEC.listOf().optionalFieldOf("entity_attribute_modifiers", List.of()).forGetter(x -> x.entityAttributeModifiers),
				Codec.STRING.optionalFieldOf("triggered_block_at_start", "").forGetter(x -> x.triggeredBlockAtStart),
				Codec.STRING.optionalFieldOf("triggered_block_at_end", "").forGetter(x -> x.triggeredBlockAtEnd)
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

			public static final Codec<AttributeModifierEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.STRING.fieldOf("attribute_identifier").forGetter(x -> x.attributeIdentifier),
					Codec.STRING.fieldOf("id").forGetter(x -> x.id),
					Codec.DOUBLE.fieldOf("amount").forGetter(x -> x.amount),
					Codec.STRING.fieldOf("operation").forGetter(x -> x.operation)
			).apply(instance, AttributeModifierEntry::new));

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
