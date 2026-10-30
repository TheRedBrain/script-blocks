package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TeleporterTrapdoorBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TeleporterTrapdoorBlock> CODEC = simpleCodec(TeleporterTrapdoorBlock::new);
	protected static final VoxelShape EAST_SHAPE = Block.box(0.0, 0.0, 0.0, 3.0, 16.0, 16.0);
	protected static final VoxelShape WEST_SHAPE = Block.box(13.0, 0.0, 0.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape SOUTH_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 3.0);
	protected static final VoxelShape NORTH_SHAPE = Block.box(0.0, 0.0, 13.0, 16.0, 16.0, 16.0);
	protected static final VoxelShape OPEN_BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);
	protected static final VoxelShape OPEN_TOP_SHAPE = Block.box(0.0, 13.0, 0.0, 16.0, 16.0, 16.0);

	public TeleporterTrapdoorBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.OPEN, false)
				.setValue(BlockStateProperties.HALF, Half.BOTTOM)
				.setValue(BlockStateProperties.WATERLOGGED, false)
		);
	}

	public MapCodec<TeleporterTrapdoorBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HALF, HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
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
		blockState = ctx.replacingClickedOnBlock() || !direction.getAxis().isHorizontal() ? blockState.setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite()).setValue(BlockStateProperties.HALF, direction == Direction.UP ? Half.BOTTOM : Half.TOP) : (BlockState) ((BlockState) blockState.setValue(HorizontalDirectionalBlock.FACING, direction)).setValue(BlockStateProperties.HALF, ctx.getClickLocation().y - (double) ctx.getClickedPos().getY() > 0.5 ? Half.TOP : Half.BOTTOM);
		return blockState.setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				if (player.isShiftKeyDown()) {
					state = state.cycle(BlockStateProperties.OPEN);
					world.setBlock(pos, state, Block.UPDATE_CLIENTS);
					if (state.getValue(BlockStateProperties.WATERLOGGED)) {
						world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
					}
					return InteractionResult.SUCCESS;
				}
				((DuckPlayerEntityMixin) player).scriptblocks$openCreativeTeleporterBlockScreen(teleporterBlockEntity);
				return InteractionResult.SUCCESS;
			} else if (player instanceof ServerPlayer serverPlayerEntity) {
				TeleporterBlockEntity.openNonCreativeScreen(world, serverPlayerEntity, pos, state, teleporterBlockEntity);
			}
		}
		return InteractionResult.CONSUME;
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
}
