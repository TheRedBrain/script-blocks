package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InteractiveTriggerDoorBlock extends InteractiveTriggerBlock {
	public static final MapCodec<InteractiveTriggerDoorBlock> CODEC = simpleCodec(InteractiveTriggerDoorBlock::new);
	protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape EAST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

	public InteractiveTriggerDoorBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.OPEN, false)
				.setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT)
				.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
				.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	public MapCodec<InteractiveTriggerDoorBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF, HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.DOOR_HINGE, BlockStateProperties.TRIGGERED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
		boolean bl = !state.getValue(BlockStateProperties.OPEN);
		boolean bl2 = state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.RIGHT;
		switch (direction) {
			default: {
				return bl ? WEST_SHAPE : (bl2 ? SOUTH_SHAPE : NORTH_SHAPE);
			}
			case SOUTH: {
				return bl ? NORTH_SHAPE : (bl2 ? WEST_SHAPE : EAST_SHAPE);
			}
			case WEST: {
				return bl ? EAST_SHAPE : (bl2 ? NORTH_SHAPE : SOUTH_SHAPE);
			}
			case NORTH:
		}
		return bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
	}

	@Override
	protected BlockState updateShape(
			final BlockState state,
			final LevelReader level,
			final ScheduledTickAccess ticks,
			final BlockPos pos,
			final Direction directionToNeighbour,
			final BlockPos neighbourPos,
			final BlockState neighbourState,
			final RandomSource random
	) {
		DoubleBlockHalf half = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
		if (directionToNeighbour.getAxis() != Direction.Axis.Y || half == DoubleBlockHalf.LOWER != (directionToNeighbour == Direction.UP)) {
			return half == DoubleBlockHalf.LOWER && directionToNeighbour == Direction.DOWN && !state.canSurvive(level, pos)
					? Blocks.AIR.defaultBlockState()
					: super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
		} else {
			return neighbourState.is(this) && neighbourState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != half
					? neighbourState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, half)
					: Blocks.AIR.defaultBlockState();
		}
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide() && (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
			onBreakInCreative(world, pos, state, player);
		}

		return super.playerWillDestroy(world, pos, state, player);
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		switch (type) {
			case LAND, AIR -> {
				return state.getValue(BlockStateProperties.OPEN);
			}
			case WATER -> {
				return false;
			}
		}
		return false;
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos blockPos = ctx.getClickedPos();
		Level world = ctx.getLevel();
		if (blockPos.getY() < world.getHeight() - 1 && world.getBlockState(blockPos.above()).canBeReplaced(ctx)) {
			boolean bl = world.hasNeighborSignal(blockPos) || world.hasNeighborSignal(blockPos.above());
			return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection()).setValue(BlockStateProperties.DOOR_HINGE, this.getHinge(ctx)).setValue(BlockStateProperties.OPEN, bl).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlock(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
	}

	private DoorHingeSide getHinge(final BlockPlaceContext context) {
		BlockGetter level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction placeDirection = context.getHorizontalDirection();
		BlockPos abovePos = pos.above();
		Direction leftDirection = placeDirection.getCounterClockWise();
		BlockPos leftPos = pos.relative(leftDirection);
		BlockState leftState = level.getBlockState(leftPos);
		BlockPos leftAbovePos = abovePos.relative(leftDirection);
		BlockState leftAboveState = level.getBlockState(leftAbovePos);
		Direction rightDirection = placeDirection.getClockWise();
		BlockPos rightPos = pos.relative(rightDirection);
		BlockState rightState = level.getBlockState(rightPos);
		BlockPos rightAbovePos = abovePos.relative(rightDirection);
		BlockState rightAboveState = level.getBlockState(rightAbovePos);
		int solidBlockBalance = (leftState.isCollisionShapeFullBlock(level, leftPos) ? -1 : 0)
				+ (leftAboveState.isCollisionShapeFullBlock(level, leftAbovePos) ? -1 : 0)
				+ (rightState.isCollisionShapeFullBlock(level, rightPos) ? 1 : 0)
				+ (rightAboveState.isCollisionShapeFullBlock(level, rightAbovePos) ? 1 : 0);
		boolean doorLeft = leftState.is(this) && leftState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		boolean doorRight = rightState.is(this) && rightState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		if ((!doorLeft || doorRight) && solidBlockBalance <= 0) {
			if ((!doorRight || doorLeft) && solidBlockBalance >= 0) {
				int stepX = placeDirection.getStepX();
				int stepZ = placeDirection.getStepZ();
				Vec3 clickLocation = context.getClickLocation();
				double clickX = clickLocation.x - pos.getX();
				double clickZ = clickLocation.z - pos.getZ();
				return (stepX >= 0 || !(clickZ < 0.5)) && (stepX <= 0 || !(clickZ > 0.5)) && (stepZ >= 0 || !(clickX > 0.5)) && (stepZ <= 0 || !(clickX < 0.5))
						? DoorHingeSide.LEFT
						: DoorHingeSide.RIGHT;
			} else {
				return DoorHingeSide.LEFT;
			}
		} else {
			return DoorHingeSide.RIGHT;
		}
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
			BlockPos posDown = pos.below();
			BlockState stateDown = world.getBlockState(pos.below());
			if (stateDown.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
				return InteractionResult.PASS;
			}
			return stateDown.getBlock().useWithoutItem(stateDown, world, posDown, player, hit);
		}
		return super.useWithoutItem(state, world, pos, player, hit);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos blockPos = pos.below();
		BlockState blockState = world.getBlockState(blockPos);
		if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
			return true;
		}
		return blockState.is(this);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return super.rotate(state, rotation).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)))).cycle(BlockStateProperties.DOOR_HINGE);
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)))).cycle(BlockStateProperties.DOOR_HINGE);
		}
		return state;
	}

	protected static void onBreakInCreative(Level world, BlockPos pos, BlockState state, Player player) {
		DoubleBlockHalf doubleBlockHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
		if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
			BlockPos blockPos = pos.below();
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.is(state.getBlock()) && blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
				BlockState blockState2 = blockState.getFluidState().is(Fluids.WATER) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
				world.setBlock(blockPos, blockState2, 35);
				world.levelEvent(player, 2001, blockPos, Block.getId(blockState));
			}
		}

	}

	@Override
	public void creativeSneakingInteraction(BlockState state, Level world, BlockPos pos) {
		state = state.cycle(BlockStateProperties.OPEN);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
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
