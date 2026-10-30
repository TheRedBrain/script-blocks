package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InteractiveTriggerTrapdoorBlock extends InteractiveTriggerBlock {
	public static final MapCodec<InteractiveTriggerTrapdoorBlock> CODEC = simpleCodec(InteractiveTriggerTrapdoorBlock::new);
	protected static final VoxelShape EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape OPEN_BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape OPEN_TOP_SHAPE = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

	public InteractiveTriggerTrapdoorBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.OPEN, false)
				.setValue(BlockStateProperties.HALF, Half.BOTTOM)
				.setValue(BlockStateProperties.WATERLOGGED, false)
				.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	public MapCodec<InteractiveTriggerTrapdoorBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HALF, HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.WATERLOGGED, BlockStateProperties.TRIGGERED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (!state.getValue(BlockStateProperties.OPEN)) {
			return state.getValue(BlockStateProperties.HALF) == Half.TOP ? OPEN_TOP_SHAPE : OPEN_BOTTOM_SHAPE;
		}
		switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
			default: {
				return NORTH_SHAPE;
			}
			case SOUTH: {
				return SOUTH_SHAPE;
			}
			case WEST: {
				return WEST_SHAPE;
			}
			case EAST:
		}
		return EAST_SHAPE;
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		switch (type) {
			case LAND, AIR -> {
				return state.getValue(BlockStateProperties.OPEN);
			}
			case WATER -> {
				return state.getValue(BlockStateProperties.WATERLOGGED);
			}
		}
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState blockState = this.defaultBlockState();
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		Direction direction = ctx.getClickedFace();
		blockState = ctx.replacingClickedOnBlock() || !direction.getAxis().isHorizontal() ? blockState.setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP) : blockState.setValue(HorizontalDirectionalBlock.FACING, direction).setValue(BlockStateProperties.HALF, ctx.getClickLocation().y - (double) ctx.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
		return blockState.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			return Fluids.WATER.getSource(false);
		}
		return super.getFluidState(state);
	}

	@Override
	protected BlockState updateShape(final BlockState state, final LevelReader level, final ScheduledTickAccess ticks, final BlockPos pos, final Direction directionToNeighbour, final BlockPos neighbourPos, final BlockState neighbourState, final RandomSource random) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return super.rotate(state, rotation).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING))));
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING))));
		}
		return state;
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, Level world, BlockPos pos) {
		state = state.cycle(BlockStateProperties.OPEN);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.getValue(BlockStateProperties.TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, Level world, BlockPos pos) {
		boolean newOpenState = state.getValue(BlockStateProperties.OPEN);
		if (!state.getValue(BlockStateProperties.TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.setValue(BlockStateProperties.OPEN, newOpenState).setValue(BlockStateProperties.TRIGGERED, true);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

	@Override
	public void reset(BlockState state, Level world, BlockPos pos) {
		boolean newOpenState = state.getValue(BlockStateProperties.OPEN);
		if (state.getValue(BlockStateProperties.TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.setValue(BlockStateProperties.OPEN, newOpenState).setValue(BlockStateProperties.TRIGGERED, false);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

}
