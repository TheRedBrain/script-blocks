package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class UseRelayAnvilBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayAnvilBlock> CODEC = simpleCodec(UseRelayAnvilBlock::new);
	private static final VoxelShape BASE_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 4.0, 14.0);
	private static final VoxelShape X_STEP_SHAPE = Block.box(3.0, 4.0, 4.0, 13.0, 5.0, 12.0);
	private static final VoxelShape X_STEM_SHAPE = Block.box(4.0, 5.0, 6.0, 12.0, 10.0, 10.0);
	private static final VoxelShape X_FACE_SHAPE = Block.box(0.0, 10.0, 3.0, 16.0, 16.0, 13.0);
	private static final VoxelShape Z_STEP_SHAPE = Block.box(4.0, 4.0, 3.0, 12.0, 5.0, 13.0);
	private static final VoxelShape Z_STEM_SHAPE = Block.box(6.0, 5.0, 4.0, 10.0, 10.0, 12.0);
	private static final VoxelShape Z_FACE_SHAPE = Block.box(3.0, 10.0, 0.0, 13.0, 16.0, 16.0);
	private static final VoxelShape X_AXIS_SHAPE = Shapes.or(BASE_SHAPE, X_STEP_SHAPE, X_STEM_SHAPE, X_FACE_SHAPE);
	private static final VoxelShape Z_AXIS_SHAPE = Shapes.or(BASE_SHAPE, Z_STEP_SHAPE, Z_STEM_SHAPE, Z_FACE_SHAPE);

	public UseRelayAnvilBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
		);
	}

	public MapCodec<UseRelayAnvilBlock> codec() {
		return CODEC;
	}

	@Override
	public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, ctx.getHorizontalDirection().getClockWise());
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayBlockScreen(useRelayBlockEntity);
				return InteractionResult.SUCCESS;
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
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
		return direction.getAxis() == Direction.Axis.X ? X_AXIS_SHAPE : Z_AXIS_SHAPE;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return super.rotate(state, rotation).setValue(HorizontalDirectionalBlock.FACING, rotation.rotate(state.getValue(HorizontalDirectionalBlock.FACING)));
	}

	@Override
	public boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

}
