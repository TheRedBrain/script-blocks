package com.github.theredbrain.scriptblocks.mixin.server.network;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerInteractionManager.class, priority = 950)
public abstract class ServerPlayerInteractionManagerMixin {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Shadow
	protected ServerWorld world;

	@Shadow
	private GameMode gameMode;

	@WrapOperation(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"))
	public boolean scriptblocks$wrap_isCreative(ServerPlayerInteractionManager instance, Operation<Boolean> original) {
		return original.call(instance) || ((this.gameMode.isSurvivalLike() || this.player.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) && this.player.hasStatusEffect(ScriptBlocks.BUILDING_MODE));
	}

	@WrapMethod(method = "tryBreakBlock")
	public boolean scriptblocks$wrap_tryBreakBlock(BlockPos pos, Operation<Boolean> original) {
		if ((this.gameMode.isSurvivalLike() || this.player.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) && this.player.hasStatusEffect(ScriptBlocks.BUILDING_MODE)) {
			BlockState blockState = this.world.getBlockState(pos);
			BlockEntity blockEntity = this.world.getBlockEntity(pos);
			Block block = blockState.getBlock();
			if (block instanceof OperatorBlock && !this.player.isCreativeLevelTwoOp()) {
				this.world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL);
				return false;
			}
			block.onBreak(this.world, pos, blockState, this.player);
			boolean bl = this.world.removeBlock(pos, false);
			if (bl) {
				block.onBroken(this.world, pos, blockState);
			}
			ItemStack itemStack = this.player.getMainHandStack();
			ItemStack itemStack2 = itemStack.copy();
			boolean bl2 = this.player.canHarvest(blockState);
			itemStack.postMine(this.world, blockState, pos, this.player);
			if (bl && bl2) {
				block.afterBreak(this.world, this.player, pos, blockState, blockEntity, itemStack2);
			}
			return true;
		}
		return original.call(pos);
	}
}
