package com.github.theredbrain.scriptblocks.block;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class InteractiveTriggerTorchBlock extends InteractiveTriggerBlock {
	protected static final MapCodec<SimpleParticleType> PARTICLE_TYPE_CODEC = Registries.PARTICLE_TYPE
			.getCodec()
			.<SimpleParticleType>comapFlatMap(
					particleType -> particleType instanceof SimpleParticleType simpleParticleType
							? DataResult.success(simpleParticleType)
							: DataResult.error(() -> "Not a SimpleParticleType: " + particleType),
					particleType -> particleType
			)
			.fieldOf("particle_options");
	public static final MapCodec<InteractiveTriggerTorchBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(PARTICLE_TYPE_CODEC.forGetter(block -> block.particle), createSettingsCodec()).apply(instance, InteractiveTriggerTorchBlock::new)
	);

	public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");

	protected static final VoxelShape SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

	protected final SimpleParticleType particle;

	public InteractiveTriggerTorchBlock(SimpleParticleType particle, Settings settings) {
		super(settings);
		this.setDefaultState(
				this.stateManager.getDefaultState().with(X_MIRRORED, false).with(Z_MIRRORED, false).with(Properties.LIT, true).with(TRIGGERED, false)
		);
		this.particle = particle;
	}

	@Override
	protected MapCodec<? extends InteractiveTriggerTorchBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected boolean canReplace(BlockState state, ItemPlacementContext context) {
		return !context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(Properties.CANDLES) < 4
				|| super.canReplace(state, context);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		return direction == Direction.DOWN && !this.canPlaceAt(state, world, pos)
				? Blocks.AIR.getDefaultState()
				: super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return sideCoversSmallSquare(world, pos.down(), Direction.UP);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.CANDLES, Properties.LIT, TRIGGERED);
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (state.get(Properties.LIT)) {
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY() + 0.7;
			double f = (double) pos.getZ() + 0.5;
			world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
			world.addParticle(this.particle, d, e, f, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = state.cycle(Properties.LIT).with(TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.get(TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(Properties.LIT);
		if (!state.get(TRIGGERED)) {
			newLitState = !newLitState;
		}
		if (!newLitState) {
			if (state.getBlock() instanceof InteractiveTriggerTorchBlock) {
				world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 0.0, 0.1, 0.0);
			}
		}
		state = state.with(Properties.LIT, newLitState).with(TRIGGERED, true);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public void reset(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(Properties.LIT);
		if (state.get(TRIGGERED)) {
			newLitState = !newLitState;
		}
		state = state.with(Properties.LIT, newLitState).with(TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

}
