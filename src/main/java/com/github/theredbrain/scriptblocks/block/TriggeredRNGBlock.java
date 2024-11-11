package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredRNGBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TriggeredRNGBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TriggeredRNGBlock> CODEC = createCodec(TriggeredRNGBlock::new);

	public TriggeredRNGBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ROTATED, 0).with(X_MIRRORED, false).with(Z_MIRRORED, false));
	}

	public MapCodec<TriggeredRNGBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredRNGBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeredRNGBlockEntity triggeredRNGBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openTriggeredRNGBlockScreen(triggeredRNGBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
