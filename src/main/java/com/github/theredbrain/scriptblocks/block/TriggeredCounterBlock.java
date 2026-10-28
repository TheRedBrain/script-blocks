package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredCounterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TriggeredCounterBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TriggeredCounterBlock> CODEC = simpleCodec(TriggeredCounterBlock::new);

	public TriggeredCounterBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<TriggeredCounterBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredCounterBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeredCounterBlockEntity triggeredCounterBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openTriggeredCounterBlockScreen(triggeredCounterBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
