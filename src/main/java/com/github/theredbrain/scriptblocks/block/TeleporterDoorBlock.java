package com.github.theredbrain.scriptblocks.block;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf doubleBlockHalf = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
		if (direction.getAxis() == Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			return neighborState.is(this) && neighborState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != doubleBlockHalf ? state.setValue(HorizontalDirectionalBlock.FACING, neighborState.getValue(HorizontalDirectionalBlock.FACING)).setValue(BlockStateProperties.OPEN, neighborState.getValue(BlockStateProperties.OPEN)).setValue(BlockStateProperties.DOOR_HINGE, neighborState.getValue(BlockStateProperties.DOOR_HINGE)) : Blocks.AIR.defaultBlockState();
		} else {
			return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
		if (!world.isClientSide && (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
			onBreakInCreative(world, pos, state, player);
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (!world.isClientSide && !player.isCreative()) {
				Block block = state.getBlock();
				ItemStack itemStack = block.getCloneItemStack(world, pos, state);
				if (!itemStack.isEmpty()) {
					CompoundTag nbtCompound = teleporterBlockEntity.createComponentlessNbtWithIdentifyingData(world.registryAccess());
					BlockItem.setBlockEntityData(itemStack, teleporterBlockEntity.getType(), nbtCompound);
					itemStack.applyComponents(teleporterBlockEntity.collectComponents());
					ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
					itemEntity.setDefaultPickUpDelay();
					world.addFreshEntity(itemEntity);
				}
			}
		}

		return super.playerWillDestroy(world, pos, state, player);
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

	private DoorHingeSide getHinge(BlockPlaceContext ctx) {
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
		boolean bl2 = blockState3.is(this) && blockState3.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
		if ((!bl || bl2) && i <= 0) {
			if ((!bl2 || bl) && i >= 0) {
				int j = direction.getStepX();
				int k = direction.getStepZ();
				Vec3 vec3d = ctx.getClickLocation();
				double d = vec3d.x - (double) blockPos.getX();
				double e = vec3d.z - (double) blockPos.getZ();
				return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
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
