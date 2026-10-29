package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InteractiveTriggerTorchBlock extends InteractiveTriggerBlock {
	protected static final MapCodec<SimpleParticleType> PARTICLE_TYPE_CODEC = BuiltInRegistries.PARTICLE_TYPE
			.byNameCodec()
			.<SimpleParticleType>comapFlatMap(
					particleType -> particleType instanceof SimpleParticleType simpleParticleType
							? DataResult.success(simpleParticleType)
							: DataResult.error(() -> "Not a SimpleParticleType: " + particleType),
					particleType -> particleType
			)
			.fieldOf("particle_options");
	public static final MapCodec<InteractiveTriggerTorchBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(PARTICLE_TYPE_CODEC.forGetter(block -> block.particle), propertiesCodec()).apply(instance, InteractiveTriggerTorchBlock::new)
	);

	protected static final VoxelShape SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

	protected final SimpleParticleType particle;

	public InteractiveTriggerTorchBlock(SimpleParticleType particle, Properties settings) {
		super(settings);
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
						.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
						.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
						.setValue(BlockStateProperties.LIT, true)
						.setValue(BlockStateProperties.TRIGGERED, false)
		);
		this.particle = particle;
	}

	@Override
	protected MapCodec<? extends InteractiveTriggerTorchBlock> codec() {
		return CODEC;
	}

	@Override // TODO allow floating torches?
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
		return directionToNeighbour == Direction.DOWN && !this.canSurvive(state, level, pos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return canSupportCenter(world, pos.below(), Direction.UP);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.CANDLES, BlockStateProperties.LIT, BlockStateProperties.TRIGGERED);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (state.getValue(BlockStateProperties.LIT)) {
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY() + 0.7;
			double f = (double) pos.getZ() + 0.5;
			world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
			world.addParticle(this.particle, d, e, f, 0.0, 0.0, 0.0);
		}
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
			if (state.getBlock() instanceof InteractiveTriggerTorchBlock) {
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

}
