package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.RotatedBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.TriggeringTrialSpawnerBlockEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TriggeringTrialSpawnerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TriggeringTrialSpawnerBlock> CODEC = createCodec(TriggeringTrialSpawnerBlock::new);
	public static final EnumProperty<TrialSpawnerState> TRIAL_SPAWNER_STATE = Properties.TRIAL_SPAWNER_STATE;
	public static final BooleanProperty OMINOUS = Properties.OMINOUS;

	@Override
	public MapCodec<TriggeringTrialSpawnerBlock> getCodec() {
		return CODEC;
	}

	public TriggeringTrialSpawnerBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(TRIAL_SPAWNER_STATE, TrialSpawnerState.INACTIVE).with(OMINOUS, false));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(TRIAL_SPAWNER_STATE, OMINOUS);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeringTrialSpawnerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world instanceof ServerWorld serverWorld
				? validateTicker(
				type,
				EntityRegistry.TRIGGERING_TRIAL_SPAWNER,
				(worldx, pos, statex, blockEntity) -> blockEntity.getSpawner().tickServer(serverWorld, pos, (Boolean)statex.getOrEmpty(Properties.OMINOUS).orElse(false))
		)
				: validateTicker(
				type,
				EntityRegistry.TRIGGERING_TRIAL_SPAWNER,
				(worldx, pos, statex, blockEntity) -> blockEntity.getSpawner().tickClient(worldx, pos, (Boolean)statex.getOrEmpty(Properties.OMINOUS).orElse(false))
		);
	}

	@Override
	public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType options) {
		super.appendTooltip(stack, context, tooltip, options);
		Spawner.appendSpawnDataToTooltip(stack, tooltip, "spawn_data");
	}
}
