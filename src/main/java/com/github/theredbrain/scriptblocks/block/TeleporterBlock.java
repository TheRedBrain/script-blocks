package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TeleporterBlock extends RotatedBlockWithEntity implements GameMasterBlock {
	public static final MapCodec<TeleporterBlock> CODEC = simpleCodec(TeleporterBlock::new);

	public TeleporterBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<TeleporterBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openCreativeTeleporterBlockScreen(teleporterBlockEntity);
				return InteractionResult.SUCCESS;
			} else if (player instanceof ServerPlayer serverPlayerEntity) {
				TeleporterBlockEntity.openNonCreativeScreen(world, serverPlayerEntity, pos, state, teleporterBlockEntity);
			}
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}