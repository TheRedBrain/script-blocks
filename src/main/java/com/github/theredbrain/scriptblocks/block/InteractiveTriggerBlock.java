package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.InteractiveTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class InteractiveTriggerBlock extends RotatedBlockWithEntity {

	public InteractiveTriggerBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ROTATED, 0).with(X_MIRRORED, false).with(Z_MIRRORED, false));
	}

	protected abstract MapCodec<? extends InteractiveTriggerBlock> getCodec();

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new InteractiveTriggerBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof InteractiveTriggerBlockEntity interactiveTriggerBlockEntity) {
			boolean bl = player.isCreativeLevelTwoOp();
			if (player.isSneaking() && bl) {
				creativeSneakingInteraction(state, world, pos);
				return ActionResult.success(world.isClient);
			}
			if (bl) {
				((DuckPlayerEntityMixin) player).scriptblocks$openInteractiveTriggerBlockScreen(interactiveTriggerBlockEntity);
				return ActionResult.success(world.isClient);
			} else {
				if (canTrigger(state) && interactiveTriggerBlockEntity.canTrigger(player)) {
					if (!interactiveTriggerBlockEntity.getUnlockedMessage().isEmpty()) {
						player.sendMessage(Text.translatable(interactiveTriggerBlockEntity.getUnlockedMessage()));
					}
					if (!interactiveTriggerBlockEntity.getUnlockedSound().isEmpty()) {
						SoundEvent soundEvent = Registries.SOUND_EVENT.get(Identifier.of(interactiveTriggerBlockEntity.getUnlockedSound()));
						if (soundEvent != null) {
							world.playSound((PlayerEntity) null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
						}
					}
					interactiveTriggerBlockEntity.tryToConsumeKeyItem(player);
					interactiveTriggerBlockEntity.trigger();
					trigger(state, world, pos);
				} else {
					if (!interactiveTriggerBlockEntity.getLockedMessage().isEmpty()) {
						player.sendMessage(Text.translatable(interactiveTriggerBlockEntity.getLockedMessage()));
					}
					if (!interactiveTriggerBlockEntity.getLockedSound().isEmpty()) {
						SoundEvent soundEvent = Registries.SOUND_EVENT.get(Identifier.of(interactiveTriggerBlockEntity.getLockedSound()));
						if (soundEvent != null) {
							world.playSound((PlayerEntity) null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
						}
					}
				}
				return ActionResult.success(world.isClient);
			}
		}
		return ActionResult.PASS;
	}

	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {

	}

	public boolean canTrigger(BlockState state) {
		return true;
	}

	public void trigger(BlockState state, World world, BlockPos pos) {

	}

	public void reset(BlockState state, World world, BlockPos pos) {

	}
}
