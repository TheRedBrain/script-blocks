package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

public class UseRelayChestBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayChestBlock> CODEC = createCodec(UseRelayChestBlock::new);
	protected static final VoxelShape CLOSED_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
	protected static final VoxelShape OPENED_SHAPE = Block.createCuboidShape(1.0, 0.0, 1.0, 15.0, 10.0, 15.0);

	public UseRelayChestBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(ScriptBlocksBlockProperties.ROTATED, 0)
				.with(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.with(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.with(HorizontalFacingBlock.FACING, Direction.NORTH)
				.with(Properties.OPEN, false)
				.with(Properties.WATERLOGGED, false)
		);
	}

	public MapCodec<UseRelayChestBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayChestBlockEntity(pos, state);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(HorizontalFacingBlock.FACING, Properties.OPEN, Properties.WATERLOGGED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(Properties.OPEN)) {
			return OPENED_SHAPE;
		} else {
			return CLOSED_SHAPE;
		}
	}

	@Override
	public boolean canPathfindThrough(BlockState state, NavigationType type) {
		return false;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
		Direction direction = ctx.getHorizontalPlayerFacing().getOpposite();
		return this.getDefaultState().with(HorizontalFacingBlock.FACING, direction).with(Properties.WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayChestBlockEntity useRelayChestBlockEntity) {
			if (player.isCreativeLevelTwoOp()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayChestBlockScreen(useRelayChestBlockEntity);
				return ActionResult.success(world.isClient);
			} else {
				if (!state.get(Properties.OPEN)) {
					if (useRelayChestBlockEntity.canTrigger(player)) {
						if (!useRelayChestBlockEntity.getUnlockedMessage().isEmpty() && !world.isClient) {
							player.sendMessage(Text.translatable(useRelayChestBlockEntity.getUnlockedMessage()), true);
						}
						if (!useRelayChestBlockEntity.getUnlockedSound().isEmpty() && !world.isClient) {
							SoundEvent soundEvent = Registries.SOUND_EVENT.get(Identifier.of(useRelayChestBlockEntity.getUnlockedSound()));
							if (soundEvent != null) {
								world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
							}
						}
						useRelayChestBlockEntity.tryToConsumeKeyItem(player);
						world.setBlockState(pos, state.with(Properties.OPEN, true), Block.NOTIFY_LISTENERS);
						useRelayChestBlockEntity.trigger();
					} else {
						if (!useRelayChestBlockEntity.getLockedMessage().isEmpty() && !world.isClient) {
							player.sendMessage(Text.translatable(useRelayChestBlockEntity.getLockedMessage()), true);
						}
						if (!useRelayChestBlockEntity.getLockedSound().isEmpty() && !world.isClient) {
							SoundEvent soundEvent = Registries.SOUND_EVENT.get(Identifier.of(useRelayChestBlockEntity.getLockedSound()));
							if (soundEvent != null) {
								world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
							}
						}
					}
					return ActionResult.success(world.isClient);
				} else {
					BlockPos relayBlockPosOffset = useRelayChestBlockEntity.getRelayBlockPositionOffset();
					if (relayBlockPosOffset.equals(BlockPos.ORIGIN)) {
						return ActionResult.PASS;
					}
					BlockPos relayBlockPos = pos.add(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
					BlockState relayBlockState = world.getBlockState(relayBlockPos);
					return relayBlockState.getBlock().onUse(relayBlockState, world, relayBlockPos, player, hit);
				}
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.get(Properties.WATERLOGGED)) {
			return Fluids.WATER.getStill(false);
		}
		return super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(Properties.WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(HorizontalFacingBlock.FACING, rotation.rotate(state.get(HorizontalFacingBlock.FACING))).with(ScriptBlocksBlockProperties.ROTATED, BlockRotationUtils.calculateNewRotatedBlockState(state.get(ScriptBlocksBlockProperties.ROTATED), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return state.rotate(mirror.getRotation(state.get(HorizontalFacingBlock.FACING))).with(ScriptBlocksBlockProperties.X_MIRRORED, !state.get(ScriptBlocksBlockProperties.X_MIRRORED));
		} else if (mirror == BlockMirror.LEFT_RIGHT) {
			return state.rotate(mirror.getRotation(state.get(HorizontalFacingBlock.FACING))).with(ScriptBlocksBlockProperties.Z_MIRRORED, !state.get(ScriptBlocksBlockProperties.Z_MIRRORED));
		}
		return state;
	}
}
