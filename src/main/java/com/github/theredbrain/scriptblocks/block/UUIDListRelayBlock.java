package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class UUIDListRelayBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UUIDListRelayBlock> CODEC = simpleCodec(UUIDListRelayBlock::new);

	public UUIDListRelayBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<UUIDListRelayBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UUIDListRelayBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UUIDListRelayBlockEntity uuidListRelayBlockEntity && player.canUseGameMasterBlocks()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openUUIDListRelayBlockScreen(uuidListRelayBlockEntity);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
