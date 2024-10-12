package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.DataRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DataRelayBlock extends BlockWithEntity {

	public DataRelayBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState());
	}

	// TODO Block Codecs
	public MapCodec<DataRelayBlock> getCodec() {
		return null;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DataRelayBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
