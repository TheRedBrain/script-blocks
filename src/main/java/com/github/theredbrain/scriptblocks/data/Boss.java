package com.github.theredbrain.scriptblocks.data;

import org.jetbrains.annotations.Nullable;

public record Boss(String bossEntityTypeId, com.github.theredbrain.scriptblocks.data.Boss.Phase[] phases) {

    public record Phase(int bossHealthThreshold, int globalTimerThreshold, int phaseTimerThreshold, boolean triggerEndsPhase, String animationsIdentifierString, String modelIdentifierString, String textureIdentifierString, @Nullable String triggeredBlockAtStart, @Nullable String triggeredBlockAtEnd) {

    }

}
