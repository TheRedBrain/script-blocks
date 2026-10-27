package com.github.theredbrain.scriptblocks.mixin.item;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@WrapOperation(method = "useOnBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowModifyWorld:Z", opcode = Opcodes.GETFIELD))
	public boolean scriptblocks$wrap_allowModifyWorld(Abilities instance, Operation<Boolean> original, @Local(argsOnly = true) UseOnContext context) {
		Player playerEntity = context.getPlayer();
		return (original.call(instance) && playerEntity != null && (playerEntity.isCreative() || !playerEntity.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) || (playerEntity != null && playerEntity.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)));
	}
}
