package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredRedstoneBlockEntity;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TriggeredRedstoneBlock extends BaseEntityBlock {
	public static final MapCodec<TriggeredRedstoneBlock> CODEC = simpleCodec(TriggeredRedstoneBlock::new);

	public TriggeredRedstoneBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(BlockStateProperties.POWER, 0)
				.setValue(ScriptBlocksBlockProperties.TOGGLED, false)
				.setValue(BlockStateProperties.TRIGGERED, false)
		);
	}

	public MapCodec<TriggeredRedstoneBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.POWER, ScriptBlocksBlockProperties.TOGGLED, BlockStateProperties.TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredRedstoneBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (player.canUseGameMasterBlocks()) {
			int newPower = state.getValue(BlockStateProperties.POWER);
			boolean toggle = state.getValue(ScriptBlocksBlockProperties.TOGGLED);
			if (player.isShiftKeyDown()) {
				toggle = !toggle;
			} else {
				newPower += 1;
			}
			world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.POWER, newPower < 0 ? 15 : newPower > 15 ? 0 : newPower).setValue(ScriptBlocksBlockProperties.TOGGLED, toggle).setValue(BlockStateProperties.TRIGGERED, false));
			world.updateNeighborsAt(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return state.getValue(BlockStateProperties.TRIGGERED) ? state.getValue(BlockStateProperties.POWER) : 0;
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return state.getValue(BlockStateProperties.TRIGGERED) ? state.getValue(BlockStateProperties.POWER) : 0;
	}

	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(BlockStateProperties.TRIGGERED)) {
			world.setBlock(pos, (BlockState) state.setValue(BlockStateProperties.TRIGGERED, false), Block.UPDATE_ALL);
			world.updateNeighborsAt(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	public void trigger(Level world, BlockState blockState, BlockPos pos) {
		if (!blockState.getValue(BlockStateProperties.TRIGGERED)) {
			if (!blockState.getValue(ScriptBlocksBlockProperties.TOGGLED)) {
				world.scheduleTick(pos, this, 4);
			}
			world.setBlock(pos, (BlockState) blockState.setValue(BlockStateProperties.TRIGGERED, true), Block.UPDATE_ALL);
			world.updateNeighborsAt(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
		} else if (blockState.getValue(ScriptBlocksBlockProperties.TOGGLED) && blockState.getValue(BlockStateProperties.TRIGGERED)) {
			world.setBlock(pos, (BlockState) blockState.setValue(BlockStateProperties.TRIGGERED, false), Block.UPDATE_ALL);
			world.updateNeighborsAt(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}
}
