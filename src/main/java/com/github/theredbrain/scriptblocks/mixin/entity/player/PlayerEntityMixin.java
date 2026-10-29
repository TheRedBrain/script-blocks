package com.github.theredbrain.scriptblocks.mixin.entity.player;

import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements DuckPlayerEntityMixin {

	@Shadow
	public abstract boolean isCreative();

	@Shadow
	public abstract boolean isSpectator();

	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@WrapOperation(method = "blockActionRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isBlockPlacingRestricted()Z"))
	public boolean scriptblocks$wrap_isBlockBreakingRestricted(GameType instance, Operation<Boolean> original) {
		return original.call(instance) || (!this.isCreative() && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT));
	}

	@WrapMethod(method = "mayBuild")
	public boolean scriptblocks$wrap_mayBuild(Operation<Boolean> original) {
		return original.call() && !(!(this.isCreative() || this.isSpectator()) && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

	@WrapMethod(method = "mayUseItemAt")
	public boolean scriptblocks$wrap_mayUseItemAt(BlockPos pos, Direction direction, ItemStack itemStack, Operation<Boolean> original) {
		return original.call(pos, direction, itemStack) && !(!(this.isCreative() || this.isSpectator()) && this.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !this.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

}
