package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Environment(value = EnvType.CLIENT)
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Shadow
	private GameMode gameMode;

	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	protected abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

	@Shadow
	public abstract boolean breakBlock(BlockPos pos);

	@Shadow
	private int blockBreakingCooldown;

	@Shadow
	protected abstract void syncSelectedSlot();

	@WrapMethod(method = "breakBlock")
	public boolean scriptblocks$wrap_breakBlock(BlockPos pos, Operation<Boolean> original) {
		if (this.client.player != null && (this.gameMode.isSurvivalLike() || this.client.player.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) && this.client.player.hasStatusEffect(ScriptBlocks.BUILDING_MODE)) {
			ClientWorld world = this.client.world;
			if (world != null) {
				BlockState blockState = world.getBlockState(pos);
				Block block = blockState.getBlock();
				block.onBreak(world, pos, blockState, this.client.player);
				FluidState fluidState = world.getFluidState(pos);
				boolean bl = world.setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
				if (bl) {
					block.onBroken(world, pos, blockState);
				}
				return bl;
			}
		}
		return original.call(pos);
	}

	@WrapMethod(method = "attackBlock")
	public boolean scriptblocks$wrap_attackBlock(BlockPos pos, Direction direction, Operation<Boolean> original) {
		if (this.client.player != null && (this.gameMode.isSurvivalLike() || this.client.player.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) && this.client.player.hasStatusEffect(ScriptBlocks.BUILDING_MODE)) {
			Optional<BlockPos> optionalHousingBlockPos = ((DuckPlayerEntityMixin) this.client.player).scriptblocks$getCurrentHousingBlockPosition();
			boolean bl = false;
			if (optionalHousingBlockPos.isPresent() && this.client.world != null && this.client.world.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
				bl = housingBlockEntity.influenceAreaContains(pos);
			} else {
				((DuckPlayerEntityMixin) this.client.player).scriptblocks$setCurrentHousingBlockPosition(Optional.empty()); // TODO C2S packet reset position
			}
			if (bl) {
				BlockState blockState = this.client.world.getBlockState(pos);
				this.client.getTutorialManager().onBlockBreaking(this.client.world, pos, blockState, 1.0f);
				this.sendSequencedPacket(this.client.world, sequence -> {
					this.breakBlock(pos);
					return new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
				});
				this.blockBreakingCooldown = 5;
			}
			return bl;
		}
		return original.call(pos, direction);
	}

	@WrapMethod(method = "interactBlock")
	public ActionResult scriptblocks$wrap_interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, Operation<ActionResult> original) {
		if ((this.gameMode.isSurvivalLike() || player.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT)) && player.hasStatusEffect(ScriptBlocks.BUILDING_MODE)) {
			this.syncSelectedSlot();
			Optional<BlockPos> optionalHousingBlockPos = ((DuckPlayerEntityMixin) player).scriptblocks$getCurrentHousingBlockPosition();
			boolean bl = false;
			if (optionalHousingBlockPos.isPresent() && this.client.world != null && this.client.world.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
				bl = housingBlockEntity.influenceAreaContains(hitResult.getBlockPos().offset(hitResult.getSide()));
			} else {
				((DuckPlayerEntityMixin) player).scriptblocks$setCurrentHousingBlockPosition(Optional.empty()); // TODO C2S packet reset position
			}
			if (!bl) {
				return ActionResult.FAIL;
			}
		}
		return original.call(player, hand, hitResult);
	}
}
