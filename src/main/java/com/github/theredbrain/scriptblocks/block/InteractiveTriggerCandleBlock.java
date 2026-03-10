package com.github.theredbrain.scriptblocks.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.ToIntFunction;

public class InteractiveTriggerCandleBlock extends InteractiveTriggerBlock implements Waterloggable {
	public static final MapCodec<InteractiveTriggerCandleBlock> CODEC = createCodec(InteractiveTriggerCandleBlock::new);
	public static final IntProperty CANDLES = Properties.CANDLES;
	public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final BooleanProperty TRIGGERED = BooleanProperty.of("triggered");
	public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> state.get(LIT) ? 3 * (Integer)state.get(CANDLES) : 0;
	private static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS = Util.make(
			() -> {
				Int2ObjectMap<List<Vec3d>> int2ObjectMap = new Int2ObjectOpenHashMap<>();
				int2ObjectMap.defaultReturnValue(ImmutableList.of());
				int2ObjectMap.put(1, ImmutableList.of(new Vec3d(0.5, 0.5, 0.5)));
				int2ObjectMap.put(2, ImmutableList.of(new Vec3d(0.375, 0.44, 0.5), new Vec3d(0.625, 0.5, 0.44)));
				int2ObjectMap.put(3, ImmutableList.of(new Vec3d(0.5, 0.313, 0.625), new Vec3d(0.375, 0.44, 0.5), new Vec3d(0.56, 0.5, 0.44)));
				int2ObjectMap.put(
						4, ImmutableList.of(new Vec3d(0.44, 0.313, 0.56), new Vec3d(0.625, 0.44, 0.56), new Vec3d(0.375, 0.44, 0.375), new Vec3d(0.56, 0.5, 0.375))
				);
				return Int2ObjectMaps.unmodifiable(int2ObjectMap);
			}
	);
	private static final VoxelShape ONE_CANDLE_SHAPE = Block.createCuboidShape(7.0, 0.0, 7.0, 9.0, 6.0, 9.0);
	private static final VoxelShape TWO_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 11.0, 6.0, 9.0);
	private static final VoxelShape THREE_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 6.0, 10.0, 6.0, 11.0);
	private static final VoxelShape FOUR_CANDLES_SHAPE = Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 10.0);


	public InteractiveTriggerCandleBlock(Settings settings) {
		super(settings);
		this.setDefaultState(
				this.stateManager.getDefaultState().with(X_MIRRORED, false).with(Z_MIRRORED, false).with(CANDLES, 1).with(LIT, false).with(WATERLOGGED, false).with(TRIGGERED, false)
		);
	}

	@Override
	protected MapCodec<InteractiveTriggerCandleBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected boolean canReplace(BlockState state, ItemPlacementContext context) {
		return !context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(CANDLES) < 4
				? true
				: super.canReplace(state, context);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
		if (blockState.isOf(this)) {
			return blockState.cycle(CANDLES);
		} else {
			FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
			boolean bl = fluidState.getFluid() == Fluids.WATER;
			return super.getPlacementState(ctx).with(WATERLOGGED, bl);
		}
	}

	@Override
	protected BlockState getStateForNeighborUpdate(
			BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos
	) {
		if ((Boolean)state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		switch (state.get(CANDLES)) {
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
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(CANDLES, LIT, WATERLOGGED, TRIGGERED);
	}

	@Override
	public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!(Boolean)state.get(WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
			BlockState blockState = state.with(WATERLOGGED, Boolean.valueOf(true));
			if ((Boolean)state.get(LIT)) {
				extinguish(null, blockState, world, pos);
			} else {
				world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
			}

			world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
			return true;
		} else {
			return false;
		}
	}

	public static boolean canBeLit(BlockState state) {
		return state.isIn(BlockTags.CANDLES, statex -> statex.contains(LIT) && statex.contains(WATERLOGGED))
				&& !(Boolean)state.get(LIT)
				&& !(Boolean)state.get(WATERLOGGED);
	}

	private Iterable<Vec3d> getParticleOffsets(BlockState state) {
		return (Iterable<Vec3d>)CANDLES_TO_PARTICLE_OFFSETS.get(((Integer)state.get(CANDLES)).intValue());
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if ((Boolean)state.get(LIT)) {
			this.getParticleOffsets(state)
					.forEach(offset -> spawnCandleParticles(world, offset.add((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), random));
		}
	}

	private static void spawnCandleParticles(World world, Vec3d vec3d, Random random) {
		float f = random.nextFloat();
		if (f < 0.3F) {
			world.addParticle(ParticleTypes.SMOKE, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
			if (f < 0.17F) {
				world.playSound(
						vec3d.x + 0.5,
						vec3d.y + 0.5,
						vec3d.z + 0.5,
						SoundEvents.BLOCK_CANDLE_AMBIENT,
						SoundCategory.BLOCKS,
						1.0F + random.nextFloat(),
						random.nextFloat() * 0.7F + 0.3F,
						false
				);
			}
		}

		world.addParticle(ParticleTypes.SMALL_FLAME, vec3d.x, vec3d.y, vec3d.z, 0.0, 0.0, 0.0);
	}

	public static void extinguish(@Nullable PlayerEntity player, BlockState state, WorldAccess world, BlockPos pos) {
		setLit(world, state, pos, false);
		if (state.getBlock() instanceof InteractiveTriggerCandleBlock) {
			((InteractiveTriggerCandleBlock)state.getBlock())
					.getParticleOffsets(state)
					.forEach(
							offset -> world.addParticle(
									ParticleTypes.SMOKE, (double)pos.getX() + offset.getX(), (double)pos.getY() + offset.getY(), (double)pos.getZ() + offset.getZ(), 0.0, 0.1F, 0.0
							)
					);
		}

		world.playSound(null, pos, SoundEvents.BLOCK_CANDLE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
		world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	private static void setLit(WorldAccess world, BlockState state, BlockPos pos, boolean lit) {
		world.setBlockState(pos, state.with(LIT, Boolean.valueOf(lit)), Block.NOTIFY_ALL_AND_REDRAW);
	}

	@Override
	public void creativeSneakingInteraction(BlockState state, World world, BlockPos pos) {
		state = (BlockState) state.cycle(LIT).with(TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
	}

	@Override
	public boolean canTrigger(BlockState state) {
		return !state.get(TRIGGERED);
	}

	@Override
	public void trigger(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(LIT);
		if (!state.get(TRIGGERED)) {
			newLitState = !newLitState;
		}
		state = state.with(LIT, newLitState).with(TRIGGERED, true);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

	@Override
	public void reset(BlockState state, World world, BlockPos pos) {
		boolean newLitState = state.get(LIT);
		if (state.get(TRIGGERED)) {
			newLitState = !newLitState;
		}
		state = state.with(LIT, newLitState).with(TRIGGERED, false);
		world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
	}

}
