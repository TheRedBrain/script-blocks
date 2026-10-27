package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayLecternBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class UseRelayLecternBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayLecternBlock> CODEC = simpleCodec(UseRelayLecternBlock::new);
	public static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
	public static final VoxelShape MIDDLE_SHAPE = Block.box(4.0, 2.0, 4.0, 12.0, 14.0, 12.0);
	public static final VoxelShape BASE_SHAPE = Shapes.or(BOTTOM_SHAPE, MIDDLE_SHAPE);
	public static final VoxelShape COLLISION_SHAPE_TOP = Block.box(0.0, 15.0, 0.0, 16.0, 15.0, 16.0);
	public static final VoxelShape COLLISION_SHAPE = Shapes.or(BASE_SHAPE, COLLISION_SHAPE_TOP);
	public static final VoxelShape WEST_SHAPE = Shapes.or(
			Block.box(1.0, 10.0, 0.0, 5.333333, 14.0, 16.0),
			Block.box(5.333333, 12.0, 0.0, 9.666667, 16.0, 16.0),
			Block.box(9.666667, 14.0, 0.0, 14.0, 18.0, 16.0),
			BASE_SHAPE
	);
	public static final VoxelShape NORTH_SHAPE = Shapes.or(
			Block.box(0.0, 10.0, 1.0, 16.0, 14.0, 5.333333),
			Block.box(0.0, 12.0, 5.333333, 16.0, 16.0, 9.666667),
			Block.box(0.0, 14.0, 9.666667, 16.0, 18.0, 14.0),
			BASE_SHAPE
	);
	public static final VoxelShape EAST_SHAPE = Shapes.or(
			Block.box(10.666667, 10.0, 0.0, 15.0, 14.0, 16.0),
			Block.box(6.333333, 12.0, 0.0, 10.666667, 16.0, 16.0),
			Block.box(2.0, 14.0, 0.0, 6.333333, 18.0, 16.0),
			BASE_SHAPE
	);
	public static final VoxelShape SOUTH_SHAPE = Shapes.or(
			Block.box(0.0, 10.0, 10.666667, 16.0, 14.0, 15.0),
			Block.box(0.0, 12.0, 6.333333, 16.0, 16.0, 10.666667),
			Block.box(0.0, 14.0, 2.0, 16.0, 18.0, 6.333333),
			BASE_SHAPE
	);

	public UseRelayLecternBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
				.setValue(BlockStateProperties.HAS_BOOK, false)
		);
	}

	public MapCodec<UseRelayLecternBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayLecternBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getOpposite());
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayBlockScreen(useRelayBlockEntity);
				return InteractionResult.success(world.isClientSide);
			} else {
				BlockPos relayBlockPosOffset = useRelayBlockEntity.getRelayBlockPositionOffset();
				if (relayBlockPosOffset.equals(BlockPos.ZERO)) {
					return InteractionResult.PASS;
				}
				BlockPos relayBlockPos = pos.offset(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
				BlockState relayBlockState = world.getBlockState(relayBlockPos);
				return relayBlockState.getBlock().useWithoutItem(relayBlockState, world, relayBlockPos, player, hit);
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return COLLISION_SHAPE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case EAST -> EAST_SHAPE;
			case WEST -> WEST_SHAPE;
			default -> BASE_SHAPE;
		};
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING, BlockStateProperties.HAS_BOOK);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return super.rotate(state, rotation).setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
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
