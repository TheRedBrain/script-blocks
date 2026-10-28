package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
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

public class UseRelayDoorBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayDoorBlock> CODEC = simpleCodec(UseRelayDoorBlock::new);
	protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape EAST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);

	public UseRelayDoorBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.OPEN, false)
				.setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT)
				.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
		);
	}

	public MapCodec<UseRelayDoorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF, HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.DOOR_HINGE);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
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
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf doubleBlockHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
		if (direction.getAxis() == Direction.Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			if (neighborState.is(this) && neighborState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != doubleBlockHalf) {
				return state.setValue(HorizontalDirectionalBlock.FACING, neighborState.getValue(HorizontalDirectionalBlock.FACING)).setValue(BlockStateProperties.OPEN, neighborState.getValue(BlockStateProperties.OPEN)).setValue(BlockStateProperties.DOOR_HINGE, neighborState.getValue(BlockStateProperties.DOOR_HINGE));
			}
			return Blocks.AIR.defaultBlockState();
		}
		if (doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(world, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
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

	private DoorHingeSide getHinge(BlockPlaceContext ctx) {
		boolean bl2;
		Level blockView = ctx.getLevel();
		BlockPos blockPos = ctx.getClickedPos();
		Direction direction = ctx.getHorizontalDirection();
		BlockPos blockPos2 = blockPos.above();
		Direction direction2 = direction.getCounterClockWise();
		BlockPos blockPos3 = blockPos.relative(direction2);
		BlockState blockState = blockView.getBlockState(blockPos3);
		BlockPos blockPos4 = blockPos2.relative(direction2);
		BlockState blockState2 = blockView.getBlockState(blockPos4);
		Direction direction3 = direction.getClockWise();
		BlockPos blockPos5 = blockPos.relative(direction3);
		BlockState blockState3 = blockView.getBlockState(blockPos5);
		BlockPos blockPos6 = blockPos2.relative(direction3);
		BlockState blockState4 = blockView.getBlockState(blockPos6);
		int i = (blockState.isCollisionShapeFullBlock(blockView, blockPos3) ? -1 : 0) + (blockState2.isCollisionShapeFullBlock(blockView, blockPos4) ? -1 : 0) + (blockState3.isCollisionShapeFullBlock(blockView, blockPos5) ? 1 : 0) + (blockState4.isCollisionShapeFullBlock(blockView, blockPos6) ? 1 : 0);
		boolean bl = blockState.is(this) && blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		boolean bl3 = bl2 = blockState3.is(this) && blockState3.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		if (bl && !bl2 || i > 0) {
			return DoorHingeSide.RIGHT;
		}
		if (bl2 && !bl || i < 0) {
			return DoorHingeSide.LEFT;
		}
		int j = direction.getStepX();
		int k = direction.getStepZ();
		Vec3 vec3d = ctx.getClickLocation();
		double d = vec3d.x - (double) blockPos.getX();
		double e = vec3d.z - (double) blockPos.getZ();
		return j < 0 && e < 0.5 || j > 0 && e > 0.5 || k < 0 && d > 0.5 || k > 0 && d < 0.5 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			boolean bl = player.canUseGameMasterBlocks();
			if (player.isShiftKeyDown() && bl) {
				state = state.cycle(BlockStateProperties.OPEN);
				world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
				return InteractionResult.SUCCESS;
			}
			if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
				BlockPos posDown = pos.below();
				BlockState stateDown = world.getBlockState(pos.below());
				if (stateDown.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
					return InteractionResult.PASS;
				}
				return stateDown.getBlock().useWithoutItem(stateDown, world, posDown, player, hit);
			}
			if (bl) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayBlockScreen(useRelayBlockEntity);
				return InteractionResult.SUCCESS;
			} else {
				BlockPos relayBlockPosOffset = useRelayBlockEntity.getRelayBlockPositionOffset();
				if (relayBlockPosOffset.equals(BlockPos.ZERO)) {
					return InteractionResult.PASS;
				}
				BlockPos relayBlockPos = pos.offset(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
				BlockState relayBlockState = world.getBlockState(relayBlockPos);
				return relayBlockState.getBlock().useWithoutItem(relayBlockState, world, relayBlockPos, player, hit);
			}
		}
		return InteractionResult.PASS;
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

}
