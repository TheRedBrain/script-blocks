package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayChestBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class UseRelayChestBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayChestBlock> CODEC = simpleCodec(UseRelayChestBlock::new);
	protected static final VoxelShape CLOSED_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);
	protected static final VoxelShape OPENED_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 10.0, 15.0);

	public UseRelayChestBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.OPEN, false)
				.setValue(BlockStateProperties.WATERLOGGED, false)
		);
	}

	public MapCodec<UseRelayChestBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayChestBlockEntity(pos, state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.OPEN, BlockStateProperties.WATERLOGGED);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(BlockStateProperties.OPEN)) {
			return OPENED_SHAPE;
		} else {
			return CLOSED_SHAPE;
		}
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		Direction direction = ctx.getHorizontalDirection().getOpposite();
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction).setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayChestBlockEntity useRelayChestBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayChestBlockScreen(useRelayChestBlockEntity);
				return InteractionResult.SUCCESS;
			} else {
				if (!state.getValue(BlockStateProperties.OPEN)) {
					if (useRelayChestBlockEntity.canTrigger(player)) {
						if (!useRelayChestBlockEntity.getUnlockedMessage().isEmpty() && !world.isClientSide()) {
							player.sendOverlayMessage(Component.translatable(useRelayChestBlockEntity.getUnlockedMessage()));
						}
						if (!useRelayChestBlockEntity.getUnlockedSound().isEmpty() && !world.isClientSide()) {
							SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(useRelayChestBlockEntity.getUnlockedSound()));
							if (soundEvent != null) {
								world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
							}
						}
						useRelayChestBlockEntity.tryToConsumeKeyItem(player);
						world.setBlock(pos, state.setValue(BlockStateProperties.OPEN, true), Block.UPDATE_CLIENTS);
						useRelayChestBlockEntity.trigger();
					} else {
						if (!useRelayChestBlockEntity.getLockedMessage().isEmpty() && !world.isClientSide()) {
							player.sendOverlayMessage(Component.translatable(useRelayChestBlockEntity.getLockedMessage()));
						}
						if (!useRelayChestBlockEntity.getLockedSound().isEmpty() && !world.isClientSide()) {
							SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse(useRelayChestBlockEntity.getLockedSound()));
							if (soundEvent != null) {
								world.playSound(null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, soundEvent, SoundSource.BLOCKS, 0.5F, world.getRandom().nextFloat() * 0.1F + 0.9F);
							}
						}
					}
					return InteractionResult.SUCCESS;
				} else {
					BlockPos relayBlockPosOffset = useRelayChestBlockEntity.getRelayBlockPositionOffset();
					if (relayBlockPosOffset.equals(BlockPos.ZERO)) {
						return InteractionResult.PASS;
					}
					BlockPos relayBlockPos = pos.offset(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
					BlockState relayBlockState = world.getBlockState(relayBlockPos);
					return relayBlockState.getBlock().useWithoutItem(relayBlockState, world, relayBlockPos, player, hit);
				}
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			return Fluids.WATER.getSource(false);
		}
		return super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING))).setValue(ScriptBlocksBlockProperties.ROTATED, BlockRotationUtils.calculateNewRotatedBlockState(state.getValue(ScriptBlocksBlockProperties.ROTATED), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING))).setValue(ScriptBlocksBlockProperties.X_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.X_MIRRORED));
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return state.rotate(mirror.getRotation(state.getValue(HorizontalDirectionalBlock.FACING))).setValue(ScriptBlocksBlockProperties.Z_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED));
		}
		return state;
	}
}
