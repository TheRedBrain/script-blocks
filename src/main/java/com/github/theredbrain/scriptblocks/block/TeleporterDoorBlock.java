package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TeleporterDoorBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TeleporterDoorBlock> CODEC = simpleCodec(TeleporterDoorBlock::new);
	protected static final VoxelShape NORTH_SHAPE;
	protected static final VoxelShape SOUTH_SHAPE;
	protected static final VoxelShape EAST_SHAPE;
	protected static final VoxelShape WEST_SHAPE;

	public TeleporterDoorBlock(BlockBehaviour.Properties settings) {
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

	public MapCodec<TeleporterDoorBlock> codec() {
		return CODEC;
	}

	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.DOUBLE_BLOCK_HALF, HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.DOOR_HINGE);
	}

	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
		boolean bl = !(Boolean) state.getValue(BlockStateProperties.OPEN);
		boolean bl2 = state.getValue(BlockStateProperties.DOOR_HINGE) == DoorHingeSide.RIGHT;
		switch (direction) {
			case SOUTH -> {
				return bl ? NORTH_SHAPE : (bl2 ? WEST_SHAPE : EAST_SHAPE);
			}
			case WEST -> {
				return bl ? EAST_SHAPE : (bl2 ? NORTH_SHAPE : SOUTH_SHAPE);
			}
			case NORTH -> {
				return bl ? SOUTH_SHAPE : (bl2 ? EAST_SHAPE : WEST_SHAPE);
			}
			default -> {
				return bl ? WEST_SHAPE : (bl2 ? SOUTH_SHAPE : NORTH_SHAPE);
			}
		}
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
	public BlockState playerWillDestroy(final Level level, final BlockPos pos, final BlockState state, final Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (!level.isClientSide() && !player.isCreative()) {
				ItemStack itemStack = new ItemStack(this);
				try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), ScriptBlocks.LOGGER)) {
					TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
					BlockItem.setBlockEntityData(itemStack, teleporterBlockEntity.getType(), output);
					itemStack.applyComponents(teleporterBlockEntity.collectComponents());
					ItemEntity itemEntity = new ItemEntity(level, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
					itemEntity.setDefaultPickUpDelay();
					level.addFreshEntity(itemEntity);
				}
			}
		}

		return super.playerWillDestroy(level, pos, state, player);
	}

	public boolean isPathfindable(BlockState state, PathComputationType type) {
		switch (type) {
			case LAND:
			case AIR:
				return state.getValue(BlockStateProperties.OPEN);
			case WATER:
				return false;
			default:
				return false;
		}
	}

	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockPos blockPos = ctx.getClickedPos();
		Level world = ctx.getLevel();
		if (blockPos.getY() < world.getHeight() - 1 && world.getBlockState(blockPos.above()).canBeReplaced(ctx)) {
			boolean bl = world.hasNeighborSignal(blockPos) || world.hasNeighborSignal(blockPos.above());
			return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection()).setValue(BlockStateProperties.DOOR_HINGE, this.getHinge(ctx)).setValue(BlockStateProperties.OPEN, bl).setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
		} else {
			return null;
		}
	}

	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlock(pos.above(), state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
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

	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			boolean bl = player.canUseGameMasterBlocks();
			if (player.isShiftKeyDown() && bl) {
				state = state.cycle(BlockStateProperties.OPEN);
				world.setBlock(pos, state, 10);
				return InteractionResult.SUCCESS;
			} else {
				BlockPos relayBlockPosOffset;
				if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
					relayBlockPosOffset = pos.below();
					BlockState stateDown = world.getBlockState(pos.below());
					return stateDown.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? InteractionResult.PASS : stateDown.getBlock().useWithoutItem(stateDown, world, relayBlockPosOffset, player, hit);
				} else if (bl) {
					((DuckPlayerEntityMixin) player).scriptblocks$openCreativeTeleporterBlockScreen(teleporterBlockEntity);
					return InteractionResult.SUCCESS;
				} else if (player instanceof ServerPlayer serverPlayerEntity) {
					TeleporterBlockEntity.openNonCreativeScreen(world, serverPlayerEntity, pos, state, teleporterBlockEntity);
				}
			}
		}
		return InteractionResult.CONSUME;
	}

	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BlockPos blockPos = pos.below();
		BlockState blockState = world.getBlockState(blockPos);
		return state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? true : blockState.is(this);
	}

	public BlockState rotate(BlockState state, Rotation rotation) {
		return super.rotate(state, rotation).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), rotation));
	}

	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)))).cycle(BlockStateProperties.DOOR_HINGE);
		} else {
			return mirror == Mirror.LEFT_RIGHT ? super.mirror(state, mirror).setValue(HorizontalDirectionalBlock.FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState(state.getValue(HorizontalDirectionalBlock.FACING), mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)))).cycle(BlockStateProperties.DOOR_HINGE) : state;
		}
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

	static {
		NORTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
		SOUTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
		EAST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
		WEST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	}
}
