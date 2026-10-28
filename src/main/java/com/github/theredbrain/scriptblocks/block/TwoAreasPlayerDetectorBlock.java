package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TwoAreasPlayerDetectorBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TwoAreasPlayerDetectorBlock> CODEC = simpleCodec(TwoAreasPlayerDetectorBlock::new);

	public TwoAreasPlayerDetectorBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<TwoAreasPlayerDetectorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TwoAreasPlayerDetectorBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ScriptBlocksEntities.TWO_AREAS_PLAYER_DETECTOR_BLOCK_ENTITY, TwoAreasPlayerDetectorBlockEntity::tick);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openTwoAreasPlayerDetectorBlockScreen(twoAreasPlayerDetectorBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
