package com.github.theredbrain.scriptblocks.entity.mob;

import net.minecraft.util.math.BlockPos;

public interface DuckMobEntityMixin {

    float scriptblocks$getBossHealthThreshold();
    void scriptblocks$setBossHealthThreshold(float bossHealthThreshold);

    int scriptblocks$getBossPhase();
    void scriptblocks$setBossPhase(int bossPhase);

    BlockPos scriptblocks$getControllerBlockPos();
    void scriptblocks$setControllerBlockPos(BlockPos controllerBlockPos);

    BlockPos scriptblocks$getUseRelayBlockPos();
    void scriptblocks$setUseRelayBlockPos(BlockPos useRelayBlockPos);

}
