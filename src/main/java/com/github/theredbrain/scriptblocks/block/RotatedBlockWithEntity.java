package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public abstract class RotatedBlockWithEntity extends BaseEntityBlock {

	public RotatedBlockWithEntity(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(ScriptBlocksBlockProperties.ROTATED, 0)
				.setValue(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, false)
		);
	}

	protected abstract MapCodec<? extends RotatedBlockWithEntity> codec();

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ScriptBlocksBlockProperties.ROTATED, ScriptBlocksBlockProperties.X_MIRRORED, ScriptBlocksBlockProperties.Z_MIRRORED);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(ScriptBlocksBlockProperties.ROTATED, BlockRotationUtils.calculateNewRotatedBlockState(state.getValue(ScriptBlocksBlockProperties.ROTATED), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.FRONT_BACK) {
			return state.setValue(ScriptBlocksBlockProperties.X_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.X_MIRRORED));
		} else if (mirror == Mirror.LEFT_RIGHT) {
			return state.setValue(ScriptBlocksBlockProperties.Z_MIRRORED, !state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED));
		}
		return state;
	}
}
