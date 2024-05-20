package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class UseRelayBlockEntity extends RotatedBlockEntity {
    private BlockPos relayBlockPositionOffset = new BlockPos(0, -1, 0);
    public UseRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public UseRelayBlockEntity(BlockPos pos, BlockState state) {
        this(EntityRegistry.USE_RELAY_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putInt("relayBlockPositionOffsetX", this.relayBlockPositionOffset.getX());
        nbt.putInt("relayBlockPositionOffsetY", this.relayBlockPositionOffset.getY());
        nbt.putInt("relayBlockPositionOffsetZ", this.relayBlockPositionOffset.getZ());

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        int l = MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetX"), -48, 48);
        int m = MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetY"), -48, 48);
        int n = MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetZ"), -48, 48);
        this.relayBlockPositionOffset = new BlockPos(l, m, n);

        super.readNbt(nbt);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public boolean openScreen(PlayerEntity player) {
        if (!player.isCreativeLevelTwoOp()) {
            return false;
        }
        if (player.getEntityWorld().isClient) {
            ((DuckPlayerEntityMixin)player).scriptblocks$openUseRelayBlockScreen(this);
        }
        return true;
    }

    public BlockPos getRelayBlockPositionOffset() {
        return relayBlockPositionOffset;
    }

    public boolean setRelayBlockPositionOffset(BlockPos relayBlockPositionOffset) {
        if (relayBlockPositionOffset.getX() == 0 && relayBlockPositionOffset.getY() == 0 && relayBlockPositionOffset.getZ() == 0) {
            return false;
        }
        int x = MathHelper.clamp(relayBlockPositionOffset.getX(), -48, 48);
        int y = MathHelper.clamp(relayBlockPositionOffset.getY(), -48, 48);
        int z = MathHelper.clamp(relayBlockPositionOffset.getZ(), -48, 48);
        this.relayBlockPositionOffset = new BlockPos(x, y, z);
        return true;
    }

    @Override
    protected void onRotate(BlockState state) {
        if (state.getBlock() instanceof RotatedBlockWithEntity) {
            if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
                BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
                this.relayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.relayBlockPositionOffset, blockRotation);
                this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
            }
            if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
                this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.FRONT_BACK);
                this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
            }
            if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
                this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.LEFT_RIGHT);
                this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
            }
        }
    }
}
