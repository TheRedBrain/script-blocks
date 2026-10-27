package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.LocationCooldownStarterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class LocationCooldownStarterBlock extends BaseEntityBlock {
	public static final MapCodec<LocationCooldownStarterBlock> CODEC = simpleCodec(LocationCooldownStarterBlock::new);

	public LocationCooldownStarterBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<LocationCooldownStarterBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LocationCooldownStarterBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

}
