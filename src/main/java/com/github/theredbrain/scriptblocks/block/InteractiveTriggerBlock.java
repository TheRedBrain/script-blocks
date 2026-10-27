package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class InteractiveTriggerBlock extends RotatedBlockWithEntity {

	public InteractiveTriggerBlock(Properties settings) {
		super(settings);
	}

	protected abstract MapCodec<? extends InteractiveTriggerBlock> codec();

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InteractiveTriggerBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
			boolean bl = player.canUseGameMasterBlocks();
			if (player.isShiftKeyDown() && bl) {
				creativeSneakingInteraction(state, world, pos);
				return InteractionResult.success(world.isClientSide);
			}
			if (bl) {
				((DuckPlayerEntityMixin) player).scriptblocks$openInteractiveTriggerBlockScreen(interactiveTriggerBlockEntity);
				return InteractionResult.success(world.isClientSide);
			} else {
				if (interactiveTriggerBlockEntity.isInteractionPrevented(player)) {
					if (!interactiveTriggerBlockEntity.getInteractionPreventedMessage().isEmpty() && !world.isClientSide) {
						player.displayClientMessage(Component.translatable(interactiveTriggerBlockEntity.getInteractionPreventedMessage()), true);
					}
					if (!interactiveTriggerBlockEntity.getInteractionPreventedSound().isEmpty() && !world.isClientSide) {
						SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(interactiveTriggerBlockEntity.getInteractionPreventedSound()));
						if (soundEvent != null) {
							world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
						}
					}
				} else if (canTrigger(state) && interactiveTriggerBlockEntity.canTrigger(player)) {
					if (!interactiveTriggerBlockEntity.getUnlockedMessage().isEmpty() && !world.isClientSide) {
						player.displayClientMessage(Component.translatable(interactiveTriggerBlockEntity.getUnlockedMessage()), true);
					}
					if (!interactiveTriggerBlockEntity.getUnlockedSound().isEmpty() && !world.isClientSide) {
						SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(interactiveTriggerBlockEntity.getUnlockedSound()));
						if (soundEvent != null) {
							world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
						}
					}
					interactiveTriggerBlockEntity.tryToConsumeKeyItem(player);
					interactiveTriggerBlockEntity.triggeredOnInteraction();
					trigger(state, world, pos);
					world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
				} else {
					if (!interactiveTriggerBlockEntity.getLockedMessage().isEmpty() && !world.isClientSide) {
						player.displayClientMessage(Component.translatable(interactiveTriggerBlockEntity.getLockedMessage()), true);
					}
					if (!interactiveTriggerBlockEntity.getLockedSound().isEmpty() && !world.isClientSide) {
						SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(interactiveTriggerBlockEntity.getLockedSound()));
						if (soundEvent != null) {
							world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
						}
					}
				}
				return InteractionResult.success(world.isClientSide);
			}
		}
		return InteractionResult.PASS;
	}

	public void creativeSneakingInteraction(BlockState state, Level world, BlockPos pos) {

	}

	public boolean canTrigger(BlockState state) {
		return true;
	}

	public void trigger(BlockState state, Level world, BlockPos pos) {

	}

	public void reset(BlockState state, Level world, BlockPos pos) {

	}
}
