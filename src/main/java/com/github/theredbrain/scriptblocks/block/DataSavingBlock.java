package com.github.theredbrain.scriptblocks.block;
//
//import com.github.theredbrain.scriptblocks.block.entity.DataSavingBlockEntity;
//import com.mojang.serialization.MapCodec;
//import net.minecraft.block.BlockRenderType;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.BlockWithEntity;
//import net.minecraft.block.entity.BlockEntity;
//import net.minecraft.util.math.BlockPos;
//import org.jetbrains.annotations.Nullable;
//
//public class DataSavingBlock extends BlockWithEntity {
//
//	public DataSavingBlock(Settings settings) {
//		super(settings);
//		this.setDefaultState(this.stateManager.getDefaultState());
//	}
//
//	// TODO Block Codecs
//	public MapCodec<DataSavingBlock> getCodec() {
//		return null;
//	}
//
//	@Nullable
//	@Override
//	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
//		return new DataSavingBlockEntity(pos, state);
//	}
//
//	@Override
//	public BlockRenderType getRenderType(BlockState state) {
//		return BlockRenderType.MODEL;
//	}
//}
