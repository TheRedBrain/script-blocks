package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.DelayTriggerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DelayTriggerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<DelayTriggerBlock> CODEC = simpleCodec(DelayTriggerBlock::new);

	public DelayTriggerBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<DelayTriggerBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DelayTriggerBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ScriptBlocksEntities.DELAY_TRIGGER_BLOCK_ENTITY, DelayTriggerBlockEntity::tick);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof DelayTriggerBlockEntity delayTriggerBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openDelayTriggerBlockScreen(delayTriggerBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
