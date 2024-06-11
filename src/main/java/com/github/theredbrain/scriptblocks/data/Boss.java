package com.github.theredbrain.scriptblocks.data;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public record Boss(String bossEntityTypeId, com.github.theredbrain.scriptblocks.data.Boss.Phase[] phases) {

    public record Phase(int bossHealthThreshold, int globalTimerThreshold, int phaseTimerThreshold, boolean triggerEndsPhase, String animationsIdentifierString, String modelIdentifierString, String textureIdentifierString, List<EntityAttributeModifier> entityAttributeModifiers, @Nullable String triggeredBlockAtStart, @Nullable String triggeredBlockAtEnd) {

        public record EntityAttributeModifier(String identifier, String name, double value, int operation) {

        }
    }

}
