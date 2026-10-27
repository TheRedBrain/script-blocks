package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InteractiveTriggerCandleBlock extends InteractiveTriggerBlock implements SimpleWaterloggedBlock {
	protected static final MapCodec<SimpleParticleType> PARTICLE_TYPE_CODEC = BuiltInRegistries.PARTICLE_TYPE
			.byNameCodec()
			.<SimpleParticleType>comapFlatMap(
					particleType -> particleType instanceof SimpleParticleType simpleParticleType
							? DataResult.success(simpleParticleType)
							: DataResult.error(() -> "Not a SimpleParticleType: " + particleType),
					particleType -> particleType
			)
			.fieldOf("particle_options");
	public static final MapCodec<InteractiveTriggerCandleBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(PARTICLE_TYPE_CODEC.forGetter(block -> block.particle), propertiesCodec()).apply(instance, InteractiveTriggerCandleBlock::new)
	);
	private static final Int2ObjectMap<List<Vec3>> CANDLES_TO_PARTICLE_OFFSETS = Util.make(
			() -> {
				Int2ObjectMap<List<Vec3>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
				int2ObjectMap.defaultReturnValue(ImmutableList.of());
				int2ObjectMap.put(1, ImmutableList.of(new Vec3(0.5, 0.5, 0.5)));
				int2ObjectMap.put(2, ImmutableList.of(new Vec3(0.375, 0.44, 0.5), new Vec3(0.625, 0.5, 0.44)));
				int2ObjectMap.put(3, ImmutableList.of(new Vec3(0.5, 0.313, 0.625), new Vec3(0.375, 0.44, 0.5), new Vec3(0.56, 0.5, 0.44)));
				int2ObjectMap.put(
						4, ImmutableList.of(new Vec3(0.44, 0.313, 0.56), new Vec3(0.625, 0.44, 0.56), new Vec3(0.375, 0.44, 0.375), new Vec3(0.56, 0.5, 0.375))
				);
				return Int2ObjectMaps.unmodifiable(int2ObjectMap);
			}
	);
	private static final VoxelShape ONE_CANDLE_SHAPE = Block.box(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
	private static final VoxelShape TWO_CANDLES_SHAPE = Block.box(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
	private static final VoxelShape THREE_CANDLES_SHAPE = Block.box(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
	private static final VoxelShape FOUR_CANDLES_SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);

	protected final SimpleParticleType particle;

	public InteractiveTriggerCandleBlock(SimpleParticleType particle, Properties settings) {
		super(settings);
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
						.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
						.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
						.setValue(BlockStateProperties.CANDLES, 1)
						.setValue(BlockStateProperties.LIT, false)
						.setValue(BlockStateProperties.WATERLOGGED, false)
						.setValue(BlockStateProperties.TRIGGERED, false)
		);
		this.particle = particle;
	}

	@Override
	protected MapCodec<InteractiveTriggerCandleBlock> codec() {
		return CODEC;
	}

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
		return !context.isSecondaryUseActive() && context.getItemInHand().getItem() == this.asItem() && state.getValue(BlockStateProperties.CANDLES) < 4
				|| super.canBeReplaced(state, context);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
		if (blockState.is(this)) {
			return blockState.cycle(BlockStateProperties.CANDLES);
		} else {
			FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
			boolean bl = fluidState.getType() == Fluids.WATER;
			return super.getStateForPlacement(ctx).setValue(BlockStateProperties.WATERLOGGED, bl);
		}
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos
	) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		switch (state.getValue(BlockStateProperties.CANDLES)) {
			case 4:
				return FOUR_CANDLES_SHAPE;
			case 3:
				return THREE_CANDLES_SHAPE;
			case 2:
				return TWO_CANDLES_SHAPE;
			default:
				return ONE_CANDLE_SHAPE;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.CANDLES, BlockStateProperties.LIT, BlockStateProperties.WATERLOGGED, BlockStateProperties.TRIGGERED);
	}

	@Override
	public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!(Boolean) state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			BlockState blockState = state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE);
			if (state.getValue(BlockStateProperties.LIT)) {
				extinguish(null, blockState, world, pos);
			} else {
				world.setBlock(pos, blockState, Block.UPDATE_ALL);
			}

			world.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(world));
			return true;
		} else {
			return false;
		}
	}

	private Iterable<Vec3> getParticleOffsets(BlockState state) {
		return CANDLES_TO_PARTICLE_OFFSETS.get(state.getValue(BlockStateProperties.CANDLES).intValue());
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (state.getValue(BlockStateProperties.LIT)) {
			this.getParticleOffsets(state)
					.forEach(offset -> spawnCandleParticles(world, offset.add((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()), random));
		}
	}

	private void spawnCandleParticles(Level world, Vec3 vec3d, RandomSource random) {
		float f = random.nextFloat();
		if (f < 0.3F) {
			world.addParticle(ParticleTypes.SMOKE, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
			if (f < 0.17F) {
				world.playSound(
						vec3d.x + 0.5,
						vec3d.y + 0.5,
						vec3d.z + 0.5,
						SoundEvents.CANDLE_AMBIENT,
						SoundSource.BLOCKS,
						1.0F + random.nextFloat(),
						random.nextFloat() * 0.7F + 0.3F,
						false
				);
			}
		}

		world.addParticle(this.particle, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
	}

	public static void extinguish(@Nullable Player player, BlockState state, LevelAccessor world, BlockPos pos) {
		setLit(world, state, pos, false);
		if (state.getBlock() instanceof InteractiveTriggerCandleBlock) {
			((InteractiveTriggerCandleBlock) state.getBlock())
					.getParticleOffsets(state)
					.forEach(
							offset -> world.addParticle(
									ParticleTypes.SMOKE, (double) pos.getX() + offset.x(), (double) pos.getY() + offset.y(), (double) pos.getZ() + offset.z(), 0.0, 0.1F, 0.0
							)
					);
		}

		world.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	private static void setLit(LevelAccessor world, BlockState state, BlockPos pos, boolean lit) {
		world.setBlock(pos, state.setValue(BlockStateProperties.LIT, lit), Block.UPDATE_ALL_IMMEDIATE);
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, Level world, BlockPos pos) {
		state = state.cycle(BlockStateProperties.LIT).setValue(BlockStateProperties.TRIGGERED, false);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
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
			if (state.getBlock() instanceof InteractiveTriggerCandleBlock) {
				((InteractiveTriggerCandleBlock) state.getBlock())
						.getParticleOffsets(state)
						.forEach(
								offset -> world.addParticle(
										ParticleTypes.SMOKE, (double) pos.getX() + offset.x(), (double) pos.getY() + offset.y(), (double) pos.getZ() + offset.z(), 0.0, 0.1F, 0.0
								)
						);
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
