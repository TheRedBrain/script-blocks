package com.github.theredbrain.scriptblocks.block;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class UseRelayBlock extends RotatedBlockWithEntity {

    public UseRelayBlock(Settings settings) {
        super(settings);
    }

    // TODO Block Codecs
    public MapCodec<UseRelayBlock> getCodec() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new UseRelayBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof UseRelayBlockEntity useRelayBlockEntity) {
            if (player.isCreativeLevelTwoOp()) {
                return ((UseRelayBlockEntity) blockEntity).openScreen(player) ? ActionResult.success(world.isClient) : ActionResult.PASS;
            } else {
                BlockPos relayBlockPosOffset = useRelayBlockEntity.getRelayBlockPositionOffset();
                BlockPos relayBlockPos = pos.add(relayBlockPosOffset.getX(), relayBlockPosOffset.getY(), relayBlockPosOffset.getZ());
                BlockState relayBlockState = world.getBlockState(relayBlockPos);
                return relayBlockState.getBlock().onUse(relayBlockState, world, relayBlockPos, player, hand, hit);
            }
        }
        return ActionResult.PASS;
    }
}
