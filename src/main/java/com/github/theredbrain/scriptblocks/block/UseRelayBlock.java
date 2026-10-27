package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
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

public class UseRelayBlock extends RotatedBlockWithEntity {
	public static final MapCodec<UseRelayBlock> CODEC = simpleCodec(UseRelayBlock::new);

	public UseRelayBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<UseRelayBlock> codec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new UseRelayBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openUseRelayBlockScreen(useRelayBlockEntity);
				return InteractionResult.success(world.isClientSide);
			} else {
				BlockPos relayBlockPosOffset = useRelayBlockEntity.getRelayBlockPositionOffset();
				if (relayBlockPosOffset.equals(BlockPos.ZERO)) {
					return InteractionResult.PASS;
				}
				BlockPos relayBlockPos = pos.offset(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
				BlockState relayBlockState = world.getBlockState(relayBlockPos);
				return relayBlockState.getBlock().useWithoutItem(relayBlockState, world, relayBlockPos, player, hit);
			}
		}
		return InteractionResult.PASS;
	}
}
