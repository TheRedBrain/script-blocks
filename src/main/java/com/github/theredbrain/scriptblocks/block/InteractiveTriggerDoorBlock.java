package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class InteractiveTriggerDoorBlock extends InteractiveTriggerBlock {
	public static final MapCodec<InteractiveTriggerDoorBlock> CODEC = createCodec(InteractiveTriggerDoorBlock::new);
	protected static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape EAST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

	public InteractiveTriggerDoorBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(ScriptBlocksBlockProperties.ROTATED, 0)
				.with(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.with(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.with(HorizontalFacingBlock.FACING, Direction.NORTH)
				.with(Properties.OPEN, false)
				.with(Properties.DOOR_HINGE, DoorHinge.LEFT)
				.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
				.with(Properties.TRIGGERED, false)
		);
	}

	public MapCodec<InteractiveTriggerDoorBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.DOUBLE_BLOCK_HALF, HorizontalFacingBlock.FACING, Properties.OPEN, Properties.DOOR_HINGE, Properties.TRIGGERED);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction direction = state.get(HorizontalFacingBlock.FACING);
		boolean bl = !state.get(Properties.OPEN);
		boolean bl2 = state.get(Properties.DOOR_HINGE) == DoorHinge.RIGHT;
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
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf doubleBlockHalf = state.get(Properties.DOUBLE_BLOCK_HALF);
		if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			if (neighborState.isOf(this) && neighborState.get(Properties.DOUBLE_BLOCK_HALF) != doubleBlockHalf) {
				return state.with(HorizontalFacingBlock.FACING, neighborState.get(HorizontalFacingBlock.FACING)).with(Properties.OPEN, neighborState.get(Properties.OPEN)).with(Properties.DOOR_HINGE, neighborState.get(Properties.DOOR_HINGE)).with(Properties.TRIGGERED, neighborState.get(Properties.TRIGGERED));
			}
			return Blocks.AIR.getDefaultState();
		}
		if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
			onBreakInCreative(world, pos, state, player);
		}

		return super.onBreak(world, pos, state, player);
	}

	@Override
	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		switch (type) {
			case LAND, AIR -> {
				return state.get(Properties.OPEN);
			}
			case WATER -> {
				return false;
			}
		}
		return false;
	}

	@Override
	@Nullable
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos blockPos = ctx.getBlockPos();
		World world = ctx.getWorld();
		if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
			boolean bl = world.isReceivingRedstonePower(blockPos) || world.isReceivingRedstonePower(blockPos.up());
			return this.getDefaultState().with(HorizontalFacingBlock.FACING, ctx.getHorizontalPlayerFacing()).with(Properties.DOOR_HINGE, this.getHinge(ctx)).with(Properties.OPEN, bl).with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
		}
		return null;
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlockState(pos.up(), state.with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), Block.NOTIFY_ALL);
	}

	private DoorHinge getHinge(ItemPlacementContext ctx) {
		boolean bl2;
		World blockView = ctx.getWorld();
		BlockPos blockPos = ctx.getBlockPos();
		Direction direction = ctx.getHorizontalPlayerFacing();
		BlockPos blockPos2 = blockPos.up();
		Direction direction2 = direction.rotateYCounterclockwise();
		BlockPos blockPos3 = blockPos.offset(direction2);
		BlockState blockState = blockView.getBlockState(blockPos3);
		BlockPos blockPos4 = blockPos2.offset(direction2);
		BlockState blockState2 = blockView.getBlockState(blockPos4);
		Direction direction3 = direction.rotateYClockwise();
		BlockPos blockPos5 = blockPos.offset(direction3);
		BlockState blockState3 = blockView.getBlockState(blockPos5);
		BlockPos blockPos6 = blockPos2.offset(direction3);
		BlockState blockState4 = blockView.getBlockState(blockPos6);
		int i = (blockState.isFullCube(blockView, blockPos3) ? -1 : 0) + (blockState2.isFullCube(blockView, blockPos4) ? -1 : 0) + (blockState3.isFullCube(blockView, blockPos5) ? 1 : 0) + (blockState4.isFullCube(blockView, blockPos6) ? 1 : 0);
		boolean bl = blockState.isOf(this) && blockState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		boolean bl3 = bl2 = blockState3.isOf(this) && blockState3.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		if (bl && !bl2 || i > 0) {
			return DoorHinge.RIGHT;
		}
		if (bl2 && !bl || i < 0) {
			return DoorHinge.LEFT;
		}
		int j = direction.getOffsetX();
		int k = direction.getOffsetZ();
		Vec3d vec3d = ctx.getHitPos();
		double d = vec3d.x - (double) blockPos.getX();
		double e = vec3d.z - (double) blockPos.getZ();
		return j < 0 && e < 0.5 || j > 0 && e > 0.5 || k < 0 && d > 0.5 || k > 0 && d < 0.5 ? DoorHinge.RIGHT : DoorHinge.LEFT;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
			BlockPos posDown = pos.down();
			BlockState stateDown = world.getBlockState(pos.down());
			if (stateDown.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
				return ActionResult.PASS;
			}
			return stateDown.getBlock().onUse(stateDown, world, posDown, player, hit);
		}
		return super.onUse(state, world, pos, player, hit);
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockPos = pos.down();
		BlockState blockState = world.getBlockState(blockPos);
		if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
			return true;
		}
		return blockState.isOf(this);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return super.rotate(state, rotation).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return super.mirror(state, mirror).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), mirror.getRotation(state.get(HorizontalFacingBlock.FACING)))).cycle(Properties.DOOR_HINGE);
		} else if (mirror == BlockMirror.LEFT_RIGHT) {
			return super.mirror(state, mirror).with(HorizontalFacingBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.get(HorizontalFacingBlock.FACING), mirror.getRotation(state.get(HorizontalFacingBlock.FACING)))).cycle(Properties.DOOR_HINGE);
		}
		return state;
	}

	protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		DoubleBlockHalf doubleBlockHalf = state.get(Properties.DOUBLE_BLOCK_HALF);
		if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
			BlockPos blockPos = pos.down();
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.isOf(state.getBlock()) && blockState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
				BlockState blockState2 = blockState.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
				world.setBlockState(blockPos, blockState2, 35);
				world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
			}
		}

	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = state.cycle(Properties.OPEN);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
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
