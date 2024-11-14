package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.CopyDataBlockEntity;
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

public class CopyDataBlock extends RotatedBlockWithEntity {
	public static final MapCodec<CopyDataBlock> CODEC = createCodec(CopyDataBlock::new);

	public CopyDataBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ROTATED, 0).with(X_MIRRORED, false).with(Z_MIRRORED, false));
	}

	public MapCodec<CopyDataBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new CopyDataBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof CopyDataBlockEntity copyDataBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openCopyDataBlockScreen(copyDataBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
