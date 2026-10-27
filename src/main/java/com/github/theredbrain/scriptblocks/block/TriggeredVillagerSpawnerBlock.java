package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredVillagerSpawnerBlockEntity;
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

public class TriggeredVillagerSpawnerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TriggeredVillagerSpawnerBlock> CODEC = simpleCodec(TriggeredVillagerSpawnerBlock::new);

	public TriggeredVillagerSpawnerBlock(Properties settings) {
		super(settings);
	}

	public MapCodec<TriggeredVillagerSpawnerBlock> codec() {
		return CODEC;
	}

	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TriggeredVillagerSpawnerBlockEntity(pos, state);
	}

	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TriggeredVillagerSpawnerBlockEntity triggeredVillagerSpawnerBlockEntity) {
			if (player.canUseGameMasterBlocks()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openTriggeredVillagerSpawnerBlockScreen(triggeredVillagerSpawnerBlockEntity);
				return InteractionResult.success(world.isClientSide);
			}
		}

		return InteractionResult.PASS;
	}
}
