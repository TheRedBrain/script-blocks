package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.RedstoneTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RedstoneTriggerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<RedstoneTriggerBlock> CODEC = simpleCodec(RedstoneTriggerBlock::new);

	public RedstoneTriggerBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	public MapCodec<RedstoneTriggerBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RedstoneTriggerBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof RedstoneTriggerBlockEntity redstoneTriggerBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openRedstoneTriggerBlockScreen(redstoneTriggerBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void neighborChanged(
			final BlockState state, final Level level, final BlockPos pos, final Block block, final @org.jspecify.annotations.Nullable Orientation orientation, final boolean movedByPiston
	) {
		if (!level.isClientSide()) {
			boolean bl = level.hasNeighborSignal(pos);
			boolean bl2 = state.getValue(BlockStateProperties.TRIGGERED);
			if (bl && !bl2) {
				level.scheduleTick(pos, this, 4);
				level.setBlock(pos, state.setValue(BlockStateProperties.TRIGGERED, true), Block.UPDATE_ALL);
			} else if (!bl && bl2) {
				level.setBlock(pos, state.setValue(BlockStateProperties.TRIGGERED, false), Block.UPDATE_ALL);
			}
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		this.trigger(world, pos);
	}

	private void trigger(ServerLevel world, BlockPos pos) {
		if (world.getBlockEntity(pos) instanceof RedstoneTriggerBlockEntity redstoneTriggerBlockEntity) {
			redstoneTriggerBlockEntity.trigger();
		}
	}
}
