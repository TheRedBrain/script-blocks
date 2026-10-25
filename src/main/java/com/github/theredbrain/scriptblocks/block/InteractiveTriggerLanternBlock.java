package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class InteractiveTriggerLanternBlock extends InteractiveTriggerBlock implements Waterloggable {
	public static final MapCodec<InteractiveTriggerLanternBlock> CODEC = createCodec(InteractiveTriggerLanternBlock::new);

	protected static final VoxelShape STANDING_SHAPE = VoxelShapes.union(
			Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 7.0, 11.0), Block.createCuboidShape(6.0, 7.0, 6.0, 10.0, 9.0, 10.0)
	);
	protected static final VoxelShape HANGING_SHAPE = VoxelShapes.union(
			Block.createCuboidShape(5.0, 1.0, 5.0, 11.0, 8.0, 11.0), Block.createCuboidShape(6.0, 8.0, 6.0, 10.0, 10.0, 10.0)
	);

	public InteractiveTriggerLanternBlock(Settings settings) {
		super(settings);
		this.setDefaultState(
				this.stateManager.getDefaultState()
						.with(ScriptBlocksBlockProperties.ROTATED, 0)
						.with(ScriptBlocksBlockProperties.X_MIRRORED, false)
						.with(ScriptBlocksBlockProperties.Z_MIRRORED, false)
						.with(Properties.LIT, true)
						.with(Properties.TRIGGERED, false)
						.with(Properties.HANGING, false)
						.with(Properties.WATERLOGGED, false)
		);
	}

	@Override
	protected MapCodec<? extends InteractiveTriggerLanternBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return attachedDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos)
				? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Direction direction = attachedDirection(state).getOpposite();
		return Block.sideCoversSmallSquare(world, pos.offset(direction), direction.getOpposite());
	}

	protected static Direction attachedDirection(BlockState state) {
		return state.get(Properties.HANGING) ? Direction.DOWN : Direction.UP;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(Properties.HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.CANDLES, Properties.LIT, Properties.TRIGGERED, Properties.HANGING, Properties.WATERLOGGED);
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = state.cycle(Properties.LIT).with(Properties.TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.get(Properties.TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(Properties.LIT);
		if (!state.get(Properties.TRIGGERED)) {
			newLitState = !newLitState;
		}
		if (!newLitState) {
			if (state.getBlock() instanceof InteractiveTriggerLanternBlock) {
				world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
			}
		}
		state = state.with(Properties.LIT, newLitState).with(Properties.TRIGGERED, true);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public void reset(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(Properties.LIT);
		if (state.get(Properties.TRIGGERED)) {
			newLitState = !newLitState;
		}
		state = state.with(Properties.LIT, newLitState).with(Properties.TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.get(Properties.WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}

}
