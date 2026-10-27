package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeringTrialSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class TriggeringTrialSpawnerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TriggeringTrialSpawnerBlock> CODEC = simpleCodec(TriggeringTrialSpawnerBlock::new);

	@Override
	public MapCodec<TriggeringTrialSpawnerBlock> codec() {
		return CODEC;
	}

	public TriggeringTrialSpawnerBlock(BlockBehaviour.Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
				.setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, TrialSpawnerState.INACTIVE)
				.setValue(BlockStateProperties.OMINOUS, false)
		);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.TRIAL_SPAWNER_STATE, BlockStateProperties.OMINOUS);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeringTrialSpawnerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world instanceof ServerLevel serverWorld
				? createTickerHelper(
				type,
				ScriptBlocksEntities.TRIGGERING_TRIAL_SPAWNER,
				(worldx, pos, statex, blockEntity) -> blockEntity.getSpawner().tickServer(serverWorld, pos, (Boolean) statex.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
		)
				: createTickerHelper(
				type,
				ScriptBlocksEntities.TRIGGERING_TRIAL_SPAWNER,
				(worldx, pos, statex, blockEntity) -> blockEntity.getSpawner().tickClient(worldx, pos, (Boolean) statex.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
		);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag options) {
		super.appendTooltip(stack, context, tooltip, options);
		Spawner.appendHoverText(stack, tooltip, "spawn_data");
	}
}
