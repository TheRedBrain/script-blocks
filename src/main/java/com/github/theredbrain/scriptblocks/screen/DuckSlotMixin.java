package com.github.theredbrain.scriptblocks.screen;

public interface DuckSlotMixin {

    void scriptblocks$setX(int x);
    void scriptblocks$setY(int y);
    void scriptblocks$setDisabledOverride(boolean disabled);

    boolean scriptblocks$getDisabledOverride();
}
