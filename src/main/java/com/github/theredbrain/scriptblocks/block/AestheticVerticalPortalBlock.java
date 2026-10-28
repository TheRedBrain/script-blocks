package com.github.theredbrain.scriptblocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AestheticVerticalPortalBlock extends Block {
	public static final MapCodec<AestheticVerticalPortalBlock> CODEC = simpleCodec(AestheticVerticalPortalBlock::new);
	protected static final VoxelShape X_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	protected static final VoxelShape Z_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
	protected static final VoxelShape EMPTY_SHAPE = Shapes.empty();
	@Nullable
	private final ParticleOptions particleEffect;
	@Nullable
	private final SoundEvent soundEvent;

	@Override
	public MapCodec<AestheticVerticalPortalBlock> codec() {
		return CODEC;
	}

	public AestheticVerticalPortalBlock(Properties settings) {
		this(settings, null, null);
	}

	public AestheticVerticalPortalBlock(Properties settings, @Nullable ParticleOptions particleEffect, @Nullable SoundEvent soundEvent) {
		super(settings);
		this.particleEffect = particleEffect;
		this.soundEvent = soundEvent;
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X)
		);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (context.isHoldingItem(this.asItem()) || (context instanceof EntityCollisionContext entityShapeContext && entityShapeContext.getEntity() instanceof Player playerEntity && playerEntity.isCreative())) {
			switch (state.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
				case Z:
					return Z_SHAPE;
				case X:
				default:
					return X_SHAPE;
			}
		} else {
			return EMPTY_SHAPE;
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS, ctx.getHorizontalDirection().getClockWise().getAxis());
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (this.soundEvent != null && random.nextInt(100) == 0) {
			world.playLocalSound(
					(double) pos.getX() + 0.5,
					(double) pos.getY() + 0.5,
					(double) pos.getZ() + 0.5,
					this.soundEvent,
					SoundSource.BLOCKS,
					0.5F,
					random.nextFloat() * 0.4F + 0.8F,
					false
			);
		}

		if (this.particleEffect != null) {
			for (int i = 0; i < 4; i++) {
				double d = (double) pos.getX() + random.nextDouble();
				double e = (double) pos.getY() + random.nextDouble();
				double f = (double) pos.getZ() + random.nextDouble();
				double g = ((double) random.nextFloat() - 0.5) * 0.5;
				double h = ((double) random.nextFloat() - 0.5) * 0.5;
				double j = ((double) random.nextFloat() - 0.5) * 0.5;
				int k = random.nextInt(2) * 2 - 1;
				if (!world.getBlockState(pos.west()).is(this) && !world.getBlockState(pos.east()).is(this)) {
					d = (double) pos.getX() + 0.5 + 0.25 * (double) k;
					g = (double) (random.nextFloat() * 2.0F * (float) k);
				} else {
					f = (double) pos.getZ() + 0.5 + 0.25 * (double) k;
					j = (double) (random.nextFloat() * 2.0F * (float) k);
				}

				world.addParticle(this.particleEffect, d, e, f, g, h, j);
			}
		}
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch (state.getValue(BlockStateProperties.HORIZONTAL_AXIS)) {
					case Z:
						return state.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.X);
					case X:
						return state.setValue(BlockStateProperties.HORIZONTAL_AXIS, Direction.Axis.Z);
					default:
						return state;
				}
			default:
				return state;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.HORIZONTAL_AXIS);
	}
}
