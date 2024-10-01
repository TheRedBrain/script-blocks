package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredAdvancementCheckerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TriggeredAdvancementCheckerBlock extends RotatedBlockWithEntity {
	public TriggeredAdvancementCheckerBlock(Settings settings) {
		super(settings);
	}

	// TODO Block Codecs
	public MapCodec<TriggeredAdvancementCheckerBlock> getCodec() {
		return null;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredAdvancementCheckerBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeredAdvancementCheckerBlockEntity triggeredAdvancementCheckerBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openTriggeredAdvancementCheckerBlockScreen(triggeredAdvancementCheckerBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
