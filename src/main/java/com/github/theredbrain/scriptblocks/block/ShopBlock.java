package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.ShopBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ShopBlock extends BlockWithEntity {
	public static final MapCodec<ShopBlock> CODEC = createCodec(ShopBlock::new);

	public ShopBlock(Settings settings) {
		super(settings);
	}

	public MapCodec<ShopBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ShopBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof ShopBlockEntity shopBlockEntity) {
			if (player.isCreativeLevelTwoOp()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openShopBlockScreen(shopBlockEntity);
				return ActionResult.success(world.isClient);
			} else if (!world.isClient) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
				return ActionResult.CONSUME;
			}
		}
		return ActionResult.PASS;
	}
}
