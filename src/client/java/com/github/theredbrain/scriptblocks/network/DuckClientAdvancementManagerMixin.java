package com.github.theredbrain.scriptblocks.network;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;

public interface DuckClientAdvancementManagerMixin {
	//    AdvancementProgress scriptblocks$getAdvancementProgress(AdvancementEntry advancementEntry);
	AdvancementProgress scriptblocks$getAdvancementProgress(Advancement advancement);
}
