package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListSavingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class UUIDListSavingBlock extends BaseEntityBlock {
	public static final MapCodec<UUIDListSavingBlock> CODEC = simpleCodec(UUIDListSavingBlock::new);

	public UUIDListSavingBlock(Properties settings) {
		super(settings);
		this.registerDefaultState(this.stateDefinition.any());
	}

	public MapCodec<UUIDListSavingBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UUIDListSavingBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

}
