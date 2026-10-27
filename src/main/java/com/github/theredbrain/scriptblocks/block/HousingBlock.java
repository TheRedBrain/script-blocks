package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class HousingBlock extends RotatedBlockWithEntity {
	public static final MapCodec<HousingBlock> CODEC = simpleCodec(HousingBlock::new);

	public HousingBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<HousingBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HousingBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ScriptBlocksEntities.HOUSING_BLOCK_ENTITY, HousingBlockEntity::tick);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof HousingBlockEntity housingBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openCreativeHousingScreen(housingBlockEntity);
			} else {
				DataAttachmentHelper.setCurrentHousingBlockPosition(player, Optional.of(housingBlockEntity.getBlockPos()));
				((DuckPlayerEntityMixin) player).scriptblocks$openHousingScreen();
			}
			return InteractionResult.success(world.isClientSide());
		}
		return InteractionResult.PASS;
	}
}
