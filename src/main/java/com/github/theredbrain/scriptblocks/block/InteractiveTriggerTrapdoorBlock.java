package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
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
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
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
	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final BooleanProperty OPEN = Properties.OPEN;
	public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");
	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape OPEN_BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape OPEN_TOP_SHAPE = Block.createCuboidShape(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

	public InteractiveTriggerTrapdoorBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(ROTATED, 0).with(X_MIRRORED, false).with(Z_MIRRORED, false).with(FACING, Direction.NORTH).with(OPEN, false).with(HALF, BlockHalf.BOTTOM).with(WATERLOGGED, false).with(TRIGGERED, false));
	}

	public MapCodec<InteractiveTriggerTrapdoorBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(HALF, FACING, OPEN, WATERLOGGED, TRIGGERED);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (!state.get(OPEN).booleanValue()) {
			return state.get(HALF) == BlockHalf.TOP ? OPEN_TOP_SHAPE : OPEN_BOTTOM_SHAPE;
		}
		switch (state.get(FACING)) {
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
				return state.get(OPEN);
			}
			case WATER -> {
				return state.get(WATERLOGGED);
			}
		}
		return false;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState blockState = this.getDefaultState();
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		Direction direction = ctx.getSide();
		blockState = ctx.canReplaceExisting() || !direction.getAxis().isHorizontal() ? (BlockState) ((BlockState) blockState.with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())).with(HALF, direction == Direction.UP ? BlockHalf.BOTTOM : BlockHalf.TOP) : (BlockState) ((BlockState) blockState.with(FACING, direction)).with(HALF, ctx.getHitPos().y - (double) ctx.getBlockPos().getY() > 0.5 ? BlockHalf.TOP : BlockHalf.BOTTOM);
		return (BlockState) blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.get(WATERLOGGED)) {
			return Fluids.WATER.getStill(false);
		}
		return super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED).booleanValue()) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return super.rotate(state, rotation).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(FACING), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return super.mirror(state, mirror).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(FACING), mirror.getRotation(state.get(FACING))));
		} else if (mirror == BlockMirror.LEFT_RIGHT) {
			return super.mirror(state, mirror).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(FACING), mirror.getRotation(state.get(FACING))));
		}
		return state;
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = (BlockState) state.cycle(OPEN);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.get(TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, World world, BlockPos pos) {
		boolean newOpenState = state.get(OPEN);
		if (!state.get(TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.with(OPEN, newOpenState).with(TRIGGERED, true);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public void reset(BlockState state, World world, BlockPos pos) {
		boolean newOpenState = state.get(OPEN);
		if (state.get(TRIGGERED)) {
			newOpenState = !newOpenState;
		}
		state = state.with(OPEN, newOpenState).with(TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

}
