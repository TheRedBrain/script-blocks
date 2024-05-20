package com.github.theredbrain.scriptblocks.mixin.screen;

import com.github.theredbrain.scriptblocks.screen.DuckSlotMixin;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin implements DuckSlotMixin {

    @Shadow @Mutable public int x;

    @Shadow @Mutable public int y;

    public void scriptblocks$setX(int x) {
        this.x = x;
    }
    public void scriptblocks$setY(int y) {
        this.y = y;
    }
    @Unique
    private boolean scriptblocks$disabledOverride = false;

    @Override
    public void scriptblocks$setDisabledOverride(boolean disabled) {
        this.scriptblocks$disabledOverride = disabled;
    }

    @Override
    public boolean scriptblocks$getDisabledOverride() {
        return this.scriptblocks$disabledOverride;
    }

    @Inject(method = "isEnabled", at = @At("TAIL"), cancellable = true)
    private void scriptblocks$isEnabled(CallbackInfoReturnable<Boolean> cir) {
        if (!this.scriptblocks$disabledOverride) return;
        cir.setReturnValue(false);
    }
}
