package com.github.theredbrain.scriptblocks.client.network;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;

public interface DuckClientAdvancementManagerMixin {
//    AdvancementProgress scriptblocks$getAdvancementProgress(AdvancementEntry advancementEntry);
    AdvancementProgress scriptblocks$getAdvancementProgress(Advancement advancement);
}
