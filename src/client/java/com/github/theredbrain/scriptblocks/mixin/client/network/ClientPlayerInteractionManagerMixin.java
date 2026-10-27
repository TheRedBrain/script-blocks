package com.github.theredbrain.scriptblocks.mixin.client.network;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Environment(value = EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Shadow
	private GameType gameMode;

	@Shadow
	@Final
	private Minecraft client;

	@Shadow
	protected abstract void sendSequencedPacket(ClientLevel world, PredictiveAction packetCreator);

	@Shadow
	public abstract boolean breakBlock(BlockPos pos);

	@Shadow
	private int blockBreakingCooldown;

	@Shadow
	protected abstract void syncSelectedSlot();

	@WrapMethod(method = "breakBlock")
	public boolean scriptblocks$wrap_breakBlock(BlockPos pos, Operation<Boolean> original) {
		if (this.client.player != null && (this.gameMode.isSurvival() || this.client.player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && this.client.player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
			ClientLevel world = this.client.level;
			if (world != null) {
				BlockState blockState = world.getBlockState(pos);
				Block block = blockState.getBlock();
				block.playerWillDestroy(world, pos, blockState, this.client.player);
				FluidState fluidState = world.getFluidState(pos);
				boolean bl = world.setBlock(pos, fluidState.createLegacyBlock(), Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
				if (bl) {
					block.destroy(world, pos, blockState);
				}
				return bl;
			}
		}
		return original.call(pos);
	}

	@WrapMethod(method = "attackBlock")
	public boolean scriptblocks$wrap_attackBlock(BlockPos pos, Direction direction, Operation<Boolean> original) {
		if (this.client.player != null && (this.gameMode.isSurvival() || this.client.player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && this.client.player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
			Optional<BlockPos> optionalHousingBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(this.client.player);
			boolean bl = false;
			if (optionalHousingBlockPos.isPresent() && this.client.level != null && this.client.level.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
				bl = housingBlockEntity.influenceAreaContains(pos);
			} else {
				DataAttachmentHelper.setCurrentHousingBlockPosition(this.client.player, Optional.empty());
			}
			if (bl) {
				BlockState blockState = this.client.level.getBlockState(pos);
				this.client.getTutorial().onDestroyBlock(this.client.level, pos, blockState, 1.0f);
				this.sendSequencedPacket(this.client.level, sequence -> {
					this.breakBlock(pos);
					return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, sequence);
				});
				this.blockBreakingCooldown = 5;
			}
			return bl;
		}
		return original.call(pos, direction);
	}

	@WrapMethod(method = "interactBlock")
	public InteractionResult scriptblocks$wrap_interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, Operation<InteractionResult> original) {
		if ((this.gameMode.isSurvival() || player.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT)) && player.hasEffect(ScriptBlocksStatusEffects.BUILDING_MODE)) {
			this.syncSelectedSlot();
			Optional<BlockPos> optionalHousingBlockPos = DataAttachmentHelper.getCurrentHousingBlockPosition(player);
			boolean bl = false;
			if (optionalHousingBlockPos.isPresent() && this.client.level != null && this.client.level.getBlockEntity(optionalHousingBlockPos.get()) instanceof HousingBlockEntity housingBlockEntity) {
				bl = housingBlockEntity.influenceAreaContains(hitResult.getBlockPos().relative(hitResult.getDirection()));
			} else {
				DataAttachmentHelper.setCurrentHousingBlockPosition(player, Optional.empty());
			}
			if (!bl) {
				return InteractionResult.FAIL;
			}
		}
		return original.call(player, hand, hitResult);
	}
}
