package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;

public abstract class RotatedBlockWithEntity extends BlockWithEntity {

	public RotatedBlockWithEntity(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
				.with(ScriptBlocksBlockProperties.ROTATED, 0)
				.with(ScriptBlocksBlockProperties.X_MIRRORED, false)
				.with(ScriptBlocksBlockProperties.Z_MIRRORED, false)
		);
	}

	protected abstract MapCodec<? extends RotatedBlockWithEntity> getCodec();

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(ScriptBlocksBlockProperties.ROTATED, ScriptBlocksBlockProperties.X_MIRRORED, ScriptBlocksBlockProperties.Z_MIRRORED);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(ScriptBlocksBlockProperties.ROTATED, BlockRotationUtils.calculateNewRotatedBlockState(state.get(ScriptBlocksBlockProperties.ROTATED), rotation));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		if (mirror == BlockMirror.FRONT_BACK) {
			return state.with(ScriptBlocksBlockProperties.X_MIRRORED, !state.get(ScriptBlocksBlockProperties.X_MIRRORED));
		} else if (mirror == BlockMirror.LEFT_RIGHT) {
			return state.with(ScriptBlocksBlockProperties.Z_MIRRORED, !state.get(ScriptBlocksBlockProperties.Z_MIRRORED));
		}
		return state;
	}
}
