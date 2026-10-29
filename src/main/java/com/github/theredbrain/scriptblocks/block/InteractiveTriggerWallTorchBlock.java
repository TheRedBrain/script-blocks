package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.ScheduledTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class InteractiveTriggerWallTorchBlock extends InteractiveTriggerTorchBlock {
	public static final MapCodec<InteractiveTriggerWallTorchBlock> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(PARTICLE_TYPE_CODEC.forGetter(block -> block.particle), propertiesCodec()).apply(instance, InteractiveTriggerWallTorchBlock::new)
	);
	private static final Map<Direction, VoxelShape> BOUNDING_SHAPES = Maps.newEnumMap(
			ImmutableMap.of(
					Direction.NORTH,
					Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
					Direction.SOUTH,
					Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
					Direction.WEST,
					Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
					Direction.EAST,
					Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
			)
	);

	public InteractiveTriggerWallTorchBlock(SimpleParticleType particle, Properties settings) {
		super(particle, settings);
		this.registerDefaultState(
				this.stateDefinition.any()
						.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
						.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
						.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
						.setValue(BlockStateProperties.LIT, true)
						.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	@Override
	protected MapCodec<InteractiveTriggerWallTorchBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return getBoundingShape(state);
	}

	public static VoxelShape getBoundingShape(BlockState state) {
		return BOUNDING_SHAPES.get(state.getValue(HorizontalDirectionalBlock.FACING));
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		return canPlaceAt(world, pos, state.getValue(HorizontalDirectionalBlock.FACING));
	}

	public static boolean canPlaceAt(LevelReader world, BlockPos pos, Direction facing) {
		BlockPos blockPos = pos.relative(facing.getOpposite());
		BlockState blockState = world.getBlockState(blockPos);
		return blockState.isFaceSturdy(world, blockPos, facing);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		BlockState blockState = this.defaultBlockState();
		LevelReader worldView = ctx.getLevel();
		BlockPos blockPos = ctx.getClickedPos();
		Direction[] directions = ctx.getNearestLookingDirections();

		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				Direction direction2 = direction.getOpposite();
				blockState = blockState.setValue(HorizontalDirectionalBlock.FACING, direction2);
				if (blockState.canSurvive(worldView, blockPos)) {
					return blockState;
				}
			}
		}

		return null;
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
		return directionToNeighbour.getOpposite() == state.getValue(HorizontalDirectionalBlock.FACING) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (state.getValue(BlockStateProperties.LIT)) {
			Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
			double d = (double) pos.getX() + 0.5;
			double e = (double) pos.getY() + 0.7;
			double f = (double) pos.getZ() + 0.5;
			double g = 0.22;
			double h = 0.27;
			Direction direction2 = direction.getOpposite();
			world.addParticle(ParticleTypes.SMOKE, d + h * (double) direction2.getStepX(), e + g, f + h * (double) direction2.getStepZ(), 0.0, 0.0, 0.0);
			world.addParticle(this.particle, d + h * (double) direction2.getStepX(), e + g, f + h * (double) direction2.getStepZ(), 0.0, 0.0, 0.0);
		}
	}

	@Override
	public void trigger(BlockState state, Level world, BlockPos pos) {
		boolean newLitState = state.getValue(BlockStateProperties.LIT);
		if (!state.getValue(BlockStateProperties.TRIGGERED)) {
			newLitState = !newLitState;
		}
		if (!newLitState) {
			if (state.getBlock() instanceof InteractiveTriggerTorchBlock) {
				Direction direction = state.getValue(HorizontalDirectionalBlock.FACING).getOpposite();
				world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + 0.27 * (double) direction.getStepX(), pos.getY() + 0.7 + 0.22, pos.getZ() + 0.5 + 0.27 * (double) direction.getStepZ(), 0.0, 0.0, 0.0);
			}
		}
		state = state.setValue(BlockStateProperties.LIT, newLitState).setValue(BlockStateProperties.TRIGGERED, true);
		world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(ScriptBlocksBlockProperties.ROTATED, BlockRotationUtils.calculateNewRotatedBlockState(state.getValue(ScriptBlocksBlockProperties.ROTATED), rotation)).setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return state.setValue(ScriptBlocksBlockProperties.X_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.X_MIRRORED)).rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return state.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED)).rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING)));
		}
		return state;
	}
}
