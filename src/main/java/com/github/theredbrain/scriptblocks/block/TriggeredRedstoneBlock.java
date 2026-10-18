package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredRedstoneBlockEntity;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TriggeredRedstoneBlock extends BlockWithEntity {
	public static final MapCodec<TriggeredRedstoneBlock> CODEC = createCodec(TriggeredRedstoneBlock::new);

	public TriggeredRedstoneBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(Properties.POWER, 0)
				.with(ScriptBlocksBlockProperties.TOGGLED, false)
				.with(Properties.TRIGGERED, false)
		);
	}

	public MapCodec<TriggeredRedstoneBlock> getCodec() {
		return CODEC;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(Properties.POWER, ScriptBlocksBlockProperties.TOGGLED, Properties.TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredRedstoneBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (player.isCreativeLevelTwoOp()) {
			int newPower = state.get(Properties.POWER);
			boolean toggle = state.get(ScriptBlocksBlockProperties.TOGGLED);
			if (player.isSneaking()) {
				toggle = !toggle;
			} else {
				newPower += 1;
			}
			world.setBlockState(pos, state.with(Properties.POWER, newPower < 0 ? 15 : newPower > 15 ? 0 : newPower).with(ScriptBlocksBlockProperties.TOGGLED, toggle).with(Properties.TRIGGERED, false));
			world.updateNeighborsAlways(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAlways(pos.offset(direction), this);
			}
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}

	@Override
	protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(Properties.TRIGGERED) ? state.get(Properties.POWER) : 0;
	}

	@Override
	protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
		return state.get(Properties.TRIGGERED) ? state.get(Properties.POWER) : 0;
	}

	@Override
	protected boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		if (state.get(Properties.TRIGGERED)) {
			world.setBlockState(pos, (BlockState) state.with(Properties.TRIGGERED, false), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAlways(pos.offset(direction), this);
			}
		}
	}

	public void trigger(World world, BlockState blockState, BlockPos pos) {
		if (!blockState.get(Properties.TRIGGERED)) {
			if (!blockState.get(ScriptBlocksBlockProperties.TOGGLED)) {
				world.scheduleBlockTick(pos, this, 4);
			}
			world.setBlockState(pos, (BlockState) blockState.with(Properties.TRIGGERED, true), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAlways(pos.offset(direction), this);
			}
		} else if (blockState.get(ScriptBlocksBlockProperties.TOGGLED) && blockState.get(Properties.TRIGGERED)) {
			world.setBlockState(pos, (BlockState) blockState.with(Properties.TRIGGERED, false), Block.NOTIFY_ALL);
			world.updateNeighborsAlways(pos, this);
			for (Direction direction : Direction.values()) {
				world.updateNeighborsAlways(pos.offset(direction), this);
			}
		}
	}
}
