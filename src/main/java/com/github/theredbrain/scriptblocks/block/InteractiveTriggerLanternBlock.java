package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InteractiveTriggerLanternBlock extends InteractiveTriggerBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<InteractiveTriggerLanternBlock> CODEC = simpleCodec(InteractiveTriggerLanternBlock::new);

	protected static final VoxelShape STANDING_SHAPE = Shapes.or(
			Block.box(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), Block.box(6.0, 7.0, 6.0, 10.0, 9.0, 10.0)
	);
	protected static final VoxelShape HANGING_SHAPE = Shapes.or(
			Block.box(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), Block.box(6.0, 8.0, 6.0, 10.0, 10.0, 10.0)
	);

	public InteractiveTriggerLanternBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
						.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
						.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
						.setValue(BlockStateProperties.LIT, true)
						.setValue(BlockStateProperties.TRIGGERED, false)
						.setValue(BlockStateProperties.HANGING, false)
						.setValue(BlockStateProperties.WATERLOGGED, false)
		);
	}

	@Override
	protected MapCodec<? extends InteractiveTriggerLanternBlock> codec() {
		return CODEC;
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
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return getConnectedDirection(state).getOpposite() == directionToNeighbour && !state.canSurvive(level, pos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		Direction direction = getConnectedDirection(state).getOpposite();
		return Block.canSupportCenter(world, pos.relative(direction), direction.getOpposite());
	}

	protected static Direction getConnectedDirection(BlockState state) {
		return state.getValue(BlockStateProperties.HANGING) ? Direction.DOWN : Direction.UP;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(BlockStateProperties.HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.CANDLES, BlockStateProperties.LIT, BlockStateProperties.TRIGGERED, BlockStateProperties.HANGING, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, Level world, BlockPos pos) {
		state = state.cycle(BlockStateProperties.LIT).setValue(BlockStateProperties.TRIGGERED, false);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.getValue(BlockStateProperties.TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, Level world, BlockPos pos) {
		boolean newLitState = state.getValue(BlockStateProperties.LIT);
		if (!state.getValue(BlockStateProperties.TRIGGERED)) {
			newLitState = !newLitState;
		}
		if (!newLitState) {
			if (state.getBlock() instanceof InteractiveTriggerLanternBlock) {
				world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
			}
		}
		state = state.setValue(BlockStateProperties.LIT, newLitState).setValue(BlockStateProperties.TRIGGERED, true);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

	@Override
	public void reset(BlockState state, Level world, BlockPos pos) {
		boolean newLitState = state.getValue(BlockStateProperties.LIT);
		if (state.getValue(BlockStateProperties.TRIGGERED)) {
			newLitState = !newLitState;
		}
		state = state.setValue(BlockStateProperties.LIT, newLitState).setValue(BlockStateProperties.TRIGGERED, false);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

}
