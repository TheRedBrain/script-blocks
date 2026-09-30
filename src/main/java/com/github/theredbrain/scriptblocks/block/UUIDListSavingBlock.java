package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListSavingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class UUIDListSavingBlock extends BlockWithEntity {
	public static final MapCodec<UUIDListSavingBlock> CODEC = createCodec(UUIDListSavingBlock::new);

	public UUIDListSavingBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState());
	}

	public MapCodec<UUIDListSavingBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new UUIDListSavingBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
