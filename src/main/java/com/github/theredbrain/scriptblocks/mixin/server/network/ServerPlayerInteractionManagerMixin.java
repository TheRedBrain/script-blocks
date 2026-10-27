package com.github.theredbrain.scriptblocks.mixin.server.network;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerPlayerGameMode.class, priority = 950)
public abstract class ServerPlayerInteractionManagerMixin {

	@Shadow
	@Final
	protected ServerPlayer player;

	@Shadow
	protected ServerLevel world;

	@Shadow
	private GameType gameMode;

	@WrapOperation(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;isCreative()Z"))
	public boolean scriptblocks$wrap_isCreative(ServerPlayerGameMode instance, Operation<Boolean> original) {
		return original.call(instance) || ((this.gameMode.isSurvival() || this.player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && this.player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE));
	}

	@WrapMethod(method = "tryBreakBlock")
	public boolean scriptblocks$wrap_tryBreakBlock(BlockPos pos, Operation<Boolean> original) {
		if ((this.gameMode.isSurvival() || this.player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && this.player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
			BlockState blockState = this.world.getBlockState(pos);
			BlockEntity blockEntity = this.world.getBlockEntity(pos);
			Block block = blockState.getBlock();
			if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
				this.world.sendBlockUpdated(pos, blockState, blockState, Block.UPDATE_ALL);
				return false;
			}
			block.playerWillDestroy(this.world, pos, blockState, this.player);
			boolean bl = this.world.removeBlock(pos, false);
			if (bl) {
				block.destroy(this.world, pos, blockState);
			}
			ItemStack itemStack = this.player.getMainHandItem();
			ItemStack itemStack2 = itemStack.copy();
			boolean bl2 = this.player.hasCorrectToolForDrops(blockState);
			itemStack.mineBlock(this.world, blockState, pos, this.player);
			if (bl && bl2) {
				block.playerDestroy(this.world, this.player, pos, blockState, blockEntity, itemStack2);
			}
			return true;
		}
		return original.call(pos);
	}
}
