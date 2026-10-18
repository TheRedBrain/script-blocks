package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class InteractiveTriggerTrapdoorBlock extends InteractiveTriggerBlock {
	public static final MapCodec<InteractiveTriggerTrapdoorBlock> CODEC = createCodec(InteractiveTriggerTrapdoorBlock::new);
	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape OPEN_BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape OPEN_TOP_SHAPE = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

	public InteractiveTriggerTrapdoorBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(ScriptBlocksBlockProperties.ROTATED, 0)
				.with(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.with(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.with(HorizontalFacingBlock.FACING, Direction.NORTH)
				.with(Properties.OPEN, false)
				.with(Properties.BLOCK_HALF, BlockHalf.BOTTOM)
				.with(Properties.WATERLOGGED, false)
				.with(Properties.TRIGGERED, false)
		);
	}

	public MapCodec<InteractiveTriggerTrapdoorBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.BLOCK_HALF, HorizontalFacingBlock.FACING, Properties.OPEN, Properties.WATERLOGGED, Properties.TRIGGERED);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (!state.get(Properties.OPEN)) {
			return state.get(Properties.BLOCK_HALF) == BlockHalf.TOP ? OPEN_TOP_SHAPE : OPEN_BOTTOM_SHAPE;
		}
		switch (state.get(HorizontalFacingBlock.FACING)) {
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
	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		switch (type) {
			case LAND, AIR -> {
				return state.get(Properties.OPEN);
			}
			case WATER -> {
				return state.get(Properties.WATERLOGGED);
			}
		}
		return false;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState blockState = this.getDefaultState();
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		Direction direction = ctx.getSide();
		blockState = ctx.canReplaceExisting() || !direction.getAxis().isHorizontal() ? blockState.with(HorizontalFacingBlock.FACING, ctx.getHorizontalPlayerFacing().getOpposite()).with(Properties.BLOCK_HALF, direction == Direction.UP ? BlockHalf.BOTTOM : BlockHalf.TOP) : blockState.with(HorizontalFacingBlock.FACING, direction).with(Properties.BLOCK_HALF, ctx.getHitPos().y - (double) ctx.getBlockPos().getY() > 0.5 ? BlockHalf.TOP : BlockHalf.BOTTOM);
		return blockState.with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.get(Properties.WATERLOGGED)) {
			return Fluids.WATER.getStill(false);
		}
		return super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return super.rotate(state, rotation).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return super.mirror(state, mirror).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), mirror.getRotation(state.get(HorizontalFacingBlock.FACING))));
		} else if (mirror == BlockMirror.LEFT_RIGHT) {
			return super.mirror(state, mirror).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), mirror.getRotation(state.get(HorizontalFacingBlock.FACING))));
		}
		return state;
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = state.cycle(Properties.OPEN);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.get(Properties.TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, World world, BlockPos pos) {
		boolean newOpenState = state.get(Properties.OPEN);
		if (!state.get(Properties.TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.with(Properties.OPEN, newOpenState).with(Properties.TRIGGERED, true);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public void reset(BlockState state, World world, BlockPos pos) {
		boolean newOpenState = state.get(Properties.OPEN);
		if (state.get(Properties.TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.with(Properties.OPEN, newOpenState).with(Properties.TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

}
