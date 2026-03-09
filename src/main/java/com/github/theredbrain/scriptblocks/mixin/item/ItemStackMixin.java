package com.github.theredbrain.scriptblocks.mixin.item;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	@WrapOperation(method = "useOnBlock", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowModifyWorld:Z", opcode = Opcodes.GETFIELD))
	public boolean scriptblocks$wrap_allowModifyWorld(PlayerAbilities instance, Operation<Boolean> original, @Local(argsOnly = true) ItemUsageContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		return (original.call(instance) && playerEntity != null && (playerEntity.isCreative() || !playerEntity.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) || (playerEntity != null && playerEntity.hasStatusEffect(ScriptBlocks.BUILDING_MODE)));
	}
}
