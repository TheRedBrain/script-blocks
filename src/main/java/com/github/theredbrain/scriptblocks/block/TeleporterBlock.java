package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.OperatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TeleporterBlock extends RotatedBlockWithEntity implements OperatorBlock {
	public static final MapCodec<TeleporterBlock> CODEC = createCodec(TeleporterBlock::new);

	public TeleporterBlock(Settings settings) {
		super(settings);
	}

	public MapCodec<TeleporterBlock> getCodec() {
		return CODEC;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TeleporterBlockEntity(pos, state);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return validateTicker(type, EntityRegistry.TELEPORTER_BLOCK_ENTITY, TeleporterBlockEntity::tick);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity instanceof TeleporterBlockEntity teleporterBlockEntity) {
			if (player.isCreativeLevelTwoOp()) {
				((DuckPlayerEntityMixin) player).scriptblocks$openCreativeTeleporterBlockScreen(teleporterBlockEntity);
				return ActionResult.success(world.isClient);
			} else if (!world.isClient) {
				player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
				return ActionResult.CONSUME;
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}