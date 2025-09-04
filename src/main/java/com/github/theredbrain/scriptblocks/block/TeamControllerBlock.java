package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeamControllerBlock extends RotatedBlockWithEntity {
	public static final MapCodec<TeamControllerBlock> CODEC = createCodec(TeamControllerBlock::new);

	public TeamControllerBlock(Settings settings) {
		super(settings);
	}

	public MapCodec<TeamControllerBlock> getCodec() {
		return CODEC;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TeamControllerBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return validateTicker(type, EntityRegistry.TEAM_CONTROLLER_BLOCK_ENTITY, TeamControllerBlockEntity::tick);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeamControllerBlockEntity teamControllerBlockEntity && player.isCreativeLevelTwoOp()) {
			((DuckPlayerEntityMixin) player).scriptblocks$openTeamControllerBlockScreen(teamControllerBlockEntity);
			return ActionResult.success(world.isClient);
		}
		return ActionResult.PASS;
	}
}
