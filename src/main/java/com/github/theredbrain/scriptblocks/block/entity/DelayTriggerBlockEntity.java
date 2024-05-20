package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class DelayTriggerBlockEntity extends RotatedBlockEntity implements Triggerable {
    private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

    private int triggerDelay = 0;

    private boolean ticking = false;
    private int remainingTicks = 0;
    public DelayTriggerBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.DELAY_TRIGGER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {

        nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
        nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
        nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
        nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

        nbt.putInt("triggerDelay", this.triggerDelay);

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {

        int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
        int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
        int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
        this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

        this.triggerDelay = nbt.getInt("triggerDelay");

        super.readNbt(nbt);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public static void tick(World world, BlockPos pos, BlockState state, DelayTriggerBlockEntity blockEntity) {

        if (!world.isClient && blockEntity.ticking) {
            blockEntity.remainingTicks--;
            if (blockEntity.remainingTicks <= 0) {
                blockEntity.ticking = false;
                blockEntity.remainingTicks = 0;
                blockEntity.triggerTriggeredBlock();
            }
        }
    }

    public boolean openScreen(PlayerEntity player) {
        if (!player.isCreativeLevelTwoOp()) {
            return false;
        }
        if (player.getEntityWorld().isClient) {
            ((DuckPlayerEntityMixin)player).scriptblocks$openDelayTriggerBlockScreen(this);
        }
        return true;
    }

    public void trigger() {
        if (this.world != null) {
            this.ticking = true;
            this.remainingTicks = this.triggerDelay;
        }
    }

    public void triggerTriggeredBlock() {
        if (this.world != null) {
            BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlock.getLeft().getX(), this.pos.getY() + this.triggeredBlock.getLeft().getY(), this.pos.getZ() + this.triggeredBlock.getLeft().getZ()));
            if (blockEntity != this) {
                boolean triggeredBlockResets = this.triggeredBlock.getRight();
                if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
                    resetable.reset();
                } else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
                    triggerable.trigger();
                }
            }
        }
    }

    public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
        return this.triggeredBlock;
    }

    public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
        this.triggeredBlock = triggeredBlock;
    }

    public int getTriggerDelay() {
        return triggerDelay;
    }

    // TODO check if input is valid
    public boolean setTriggerDelay(int triggerDelay) {
        this.triggerDelay = triggerDelay;
        return true;
    }

    @Override
    protected void onRotate(BlockState state) {
        if (state.getBlock() instanceof RotatedBlockWithEntity) {
            if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
                BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
                this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
                this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
            }
            if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
                this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
                this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
            }
            if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
                this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
                this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
            }
        }
    }
}
