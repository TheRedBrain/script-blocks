package com.github.theredbrain.scriptblocks.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AestheticVerticalPortalBlock extends Block {
	public static final MapCodec<AestheticVerticalPortalBlock> CODEC = createCodec(AestheticVerticalPortalBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
	protected static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	protected static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);
	protected static final VoxelShape EMPTY_SHAPE = VoxelShapes.empty();
	@Nullable
	private final ParticleEffect particleEffect;
	@Nullable
	private final SoundEvent soundEvent;

	@Override
	public MapCodec<AestheticVerticalPortalBlock> getCodec() {
		return CODEC;
	}

	public AestheticVerticalPortalBlock(Settings settings) {
		this(settings, null, null);
	}

	public AestheticVerticalPortalBlock(Settings settings, @Nullable ParticleEffect particleEffect, @Nullable SoundEvent soundEvent) {
		super(settings);
		this.particleEffect = particleEffect;
		this.soundEvent = soundEvent;
		this.setDefaultState(this.stateManager.getDefaultState().with(AXIS, Direction.Axis.X));
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context.isHolding(this.asItem()) || (context instanceof EntityShapeContext entityShapeContext && entityShapeContext.getEntity() instanceof PlayerEntity playerEntity && playerEntity.isCreative())) {
			switch ((Direction.Axis) state.get(AXIS)) {
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
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(AXIS, ctx.getHorizontalPlayerFacing().rotateYClockwise().getAxis());
	}

	@Override
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		if (this.soundEvent != null && random.nextInt(100) == 0) {
			world.playSound(
					(double) pos.getX() + 0.5,
					(double) pos.getY() + 0.5,
					(double) pos.getZ() + 0.5,
					this.soundEvent,
					SoundCategory.BLOCKS,
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
				if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this)) {
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
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		switch (rotation) {
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:
				switch ((Direction.Axis) state.get(AXIS)) {
					case Z:
						return state.with(AXIS, Direction.Axis.X);
					case X:
						return state.with(AXIS, Direction.Axis.Z);
					default:
						return state;
				}
			default:
				return state;
		}
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}
}
