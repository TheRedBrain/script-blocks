package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.StatusEffectManipulationBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListRelayBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UUIDListSavingBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StatusEffectManipulationBlock extends BlockWithEntity {
	public static final MapCodec<StatusEffectManipulationBlock> CODEC = createCodec(StatusEffectManipulationBlock::new);

	public StatusEffectManipulationBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState());
	}

	public MapCodec<StatusEffectManipulationBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new StatusEffectManipulationBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof StatusEffectManipulationBlockEntity statusEffectManipulationBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openStatusEffectManipulationBlockScreen(statusEffectManipulationBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
