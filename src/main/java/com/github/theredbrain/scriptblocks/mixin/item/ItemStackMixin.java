package com.github.theredbrain.scriptblocks.mixin.item;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

	/**
	 * @author TheRedBrain
	 */
	@WrapOperation(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;canPlaceOn(Lnet/minecraft/block/pattern/CachedBlockPosition;)Z"))
	public boolean useOnBlock(ItemStack instance, CachedBlockPosition pos, Operation<Boolean> original, @Local(argsOnly = true) ItemUsageContext context) {
		PlayerEntity playerEntity = context.getPlayer();
		return original.call(instance, pos) || (playerEntity != null && playerEntity.hasStatusEffect(ScriptBlocks.BUILDING_MODE));
	}
}
