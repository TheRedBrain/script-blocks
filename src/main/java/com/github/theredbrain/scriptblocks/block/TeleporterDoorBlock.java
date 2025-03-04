package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class TeleporterDoorBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TeleporterDoorBlock> CODEC = createCodec(TeleporterDoorBlock::new);
	public static final DirectionProperty FACING;
	public static final BooleanProperty OPEN;
	public static final EnumProperty<DoorHinge> HINGE;
	public static final EnumProperty<DoubleBlockHalf> HALF;
	protected static final VoxelShape NORTH_SHAPE;
	protected static final VoxelShape SOUTH_SHAPE;
	protected static final VoxelShape EAST_SHAPE;
	protected static final VoxelShape WEST_SHAPE;

	public TeleporterDoorBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState()).with(ROTATED, 0)).with(X_MIRRORED, false)).with(Z_MIRRORED, false)).with(FACING, Direction.NORTH)).with(OPEN, false)).with(HINGE, DoorHinge.LEFT)).with(HALF, DoubleBlockHalf.LOWER));
	}

	public MapCodec<TeleporterDoorBlock> getCodec() {
		return CODEC;
	}

	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return validateTicker(type, EntityRegistry.TELEPORTER_BLOCK_ENTITY, TeleporterBlockEntity::tick);
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(new Property[]{HALF, FACING, OPEN, HINGE});
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		Direction direction = (Direction) state.get(FACING);
		boolean bl = !(Boolean) state.get(OPEN);
		boolean bl2 = state.get(HINGE) == DoorHinge.RIGHT;
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

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf doubleBlockHalf = (DoubleBlockHalf) state.get(HALF);
		if (direction.getAxis() == Axis.Y && doubleBlockHalf == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
			return neighborState.isOf(this) && neighborState.get(HALF) != doubleBlockHalf ? (BlockState) ((BlockState) ((BlockState) state.with(FACING, (Direction) neighborState.get(FACING))).with(OPEN, (Boolean) neighborState.get(OPEN))).with(HINGE, (DoorHinge) neighborState.get(HINGE)) : Blocks.AIR.getDefaultState();
		} else {
			return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
		}
	}

	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
			onBreakInCreative(world, pos, state, player);
		}

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (!world.isClient && !player.isCreative()) {
				Block block = state.getBlock();
				ItemStack itemStack = block.getPickStack(world, pos, state);
				if (!itemStack.isEmpty()) {
					NbtCompound nbtCompound = teleporterBlockEntity.createComponentlessNbtWithIdentifyingData(world.getRegistryManager());
					BlockItem.setBlockEntityData(itemStack, teleporterBlockEntity.getType(), nbtCompound);
					itemStack.applyComponentsFrom(teleporterBlockEntity.createComponentMap());
					ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
					itemEntity.setToDefaultPickupDelay();
					world.spawnEntity(itemEntity);
				}
			}
		}

		return super.onBreak(world, pos, state, player);
	}

	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		switch (type) {
			case LAND:
			case AIR:
				return (Boolean) state.get(OPEN);
			case WATER:
				return false;
			default:
				return false;
		}
	}

	public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockPos blockPos = ctx.getBlockPos();
		World world = ctx.getWorld();
		if (blockPos.getY() < world.getTopY() - 1 && world.getBlockState(blockPos.up()).canReplace(ctx)) {
			boolean bl = world.isReceivingRedstonePower(blockPos) || world.isReceivingRedstonePower(blockPos.up());
			return (BlockState) ((BlockState) ((BlockState) ((BlockState) this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(HINGE, this.getHinge(ctx))).with(OPEN, bl)).with(HALF, DoubleBlockHalf.LOWER);
		} else {
			return null;
		}
	}

	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		world.setBlockState(pos.up(), (BlockState) state.with(HALF, DoubleBlockHalf.UPPER), 3);
	}

	private DoorHinge getHinge(ItemPlacementContext ctx) {
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
		boolean bl = blockState.isOf(this) && blockState.get(HALF) == DoubleBlockHalf.LOWER;
		boolean bl2 = blockState3.isOf(this) && blockState3.get(HALF) == DoubleBlockHalf.LOWER;
		if ((!bl || bl2) && i <= 0) {
			if ((!bl2 || bl) && i >= 0) {
				int j = direction.getOffsetX();
				int k = direction.getOffsetZ();
				Vec3d vec3d = ctx.getHitPos();
				double d = vec3d.x - (double) blockPos.getX();
				double e = vec3d.z - (double) blockPos.getZ();
				return (j >= 0 || !(e < 0.5)) && (j <= 0 || !(e > 0.5)) && (k >= 0 || !(d > 0.5)) && (k <= 0 || !(d < 0.5)) ? DoorHinge.LEFT : DoorHinge.RIGHT;
			} else {
				return DoorHinge.LEFT;
			}
		} else {
			return DoorHinge.RIGHT;
		}
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			boolean bl = player.isCreativeLevelTwoOp();
			if (player.isSneaking() && bl) {
				state = (BlockState) state.cycle(OPEN);
				world.setBlockState(pos, state, 10);
				return ActionResult.success(world.isClient);
			} else {
				BlockPos relayBlockPosOffset;
				if (state.get(HALF) == DoubleBlockHalf.UPPER) {
					relayBlockPosOffset = pos.down();
					BlockState stateDown = world.getBlockState(pos.down());
					return stateDown.get(HALF) == DoubleBlockHalf.UPPER ? ActionResult.PASS : stateDown.getBlock().onUse(stateDown, world, relayBlockPosOffset, player, hit);
				} else if (bl) {
					((DuckPlayerEntityMixin) player).scriptblocks$openCreativeTeleporterBlockScreen(teleporterBlockEntity);
					return ActionResult.success(world.isClient);
				} else if (!world.isClient) {
					player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
					return ActionResult.CONSUME;
				}
			}
		}
		return ActionResult.PASS;
	}

	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		BlockPos blockPos = pos.down();
		BlockState blockState = world.getBlockState(blockPos);
		return state.get(HALF) == DoubleBlockHalf.LOWER ? true : blockState.isOf(this);
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return (BlockState) super.rotate(state, rotation).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState((Direction) state.get(FACING), rotation));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return (BlockState) ((BlockState) super.mirror(state, mirror).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState((Direction) state.get(FACING), mirror.getRotation((Direction) state.get(FACING))))).cycle(HINGE);
		} else {
			return mirror == BlockMirror.LEFT_RIGHT ? (BlockState) ((BlockState) super.mirror(state, mirror).with(FACING, BlockRotationUtils.calculateNewHorizontalFacingBlockState((Direction) state.get(FACING), mirror.getRotation((Direction) state.get(FACING))))).cycle(HINGE) : state;
		}
	}

	protected static void onBreakInCreative(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		DoubleBlockHalf doubleBlockHalf = (DoubleBlockHalf) state.get(HALF);
		if (doubleBlockHalf == DoubleBlockHalf.UPPER) {
			BlockPos blockPos = pos.down();
			BlockState blockState = world.getBlockState(blockPos);
			if (blockState.isOf(state.getBlock()) && blockState.get(HALF) == DoubleBlockHalf.LOWER) {
				BlockState blockState2 = blockState.getFluidState().isOf(Fluids.WATER) ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState();
				world.setBlockState(blockPos, blockState2, 35);
				world.syncWorldEvent(player, 2001, blockPos, Block.getRawIdFromState(blockState));
			}
		}

	}

	static {
		FACING = HorizontalFacingBlock.FACING;
		OPEN = Properties.OPEN;
		HINGE = Properties.DOOR_HINGE;
		HALF = Properties.DOUBLE_BLOCK_HALF;
		NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
		SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
		EAST_SHAPE = Block.createCuboidShape(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
		WEST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	}
}
