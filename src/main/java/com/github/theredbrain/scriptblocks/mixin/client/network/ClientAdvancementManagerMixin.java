package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.client.network.DuckClientAdvancementManagerMixin;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ClientAdvancementManager.class)
public class ClientAdvancementManagerMixin implements DuckClientAdvancementManagerMixin {
//    @Shadow @Final private Map<AdvancementEntry, AdvancementProgress> advancementProgresses;

    //    @Override
//    public AdvancementProgress scriptblocks$getAdvancementProgress(AdvancementEntry advancementEntry) {
//        return this.advancementProgresses.get(advancementEntry);
//    }

    @Shadow @Final private Map<Advancement, AdvancementProgress> advancementProgresses;

    @Override
    public AdvancementProgress scriptblocks$getAdvancementProgress(Advancement advancement) {
        return this.advancementProgresses.get(advancement);
    }
}
