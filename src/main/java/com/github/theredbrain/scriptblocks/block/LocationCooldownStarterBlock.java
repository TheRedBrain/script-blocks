package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.LocationCooldownStarterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class LocationCooldownStarterBlock extends BlockWithEntity {
	public static final MapCodec<LocationCooldownStarterBlock> CODEC = createCodec(LocationCooldownStarterBlock::new);

	public LocationCooldownStarterBlock(Settings settings) {
		super(settings);
	}

	public MapCodec<LocationCooldownStarterBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LocationCooldownStarterBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
