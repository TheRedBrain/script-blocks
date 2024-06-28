package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TriggeredCounterBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
    private HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
    private int counter = 0;

    public TriggeredCounterBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.TRIGGERED_COUNTER_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        List<Integer> triggeredBlocksKeys = new ArrayList<>(this.triggeredBlocks.keySet());
        nbt.putInt("triggeredBlocksKeysSize", triggeredBlocksKeys.size());
        for (int i = 0; i < triggeredBlocksKeys.size(); i++) {
            int key = triggeredBlocksKeys.get(i);
            nbt.putInt("triggeredBlocks_key_" + i, key);
            nbt.putInt("triggeredBlocks_entry_X_" + i, this.triggeredBlocks.get(key).getLeft().getX());
            nbt.putInt("triggeredBlocks_entry_Y_" + i, this.triggeredBlocks.get(key).getLeft().getY());
            nbt.putInt("triggeredBlocks_entry_Z_" + i, this.triggeredBlocks.get(key).getLeft().getZ());
            nbt.putBoolean("triggeredBlocks_entry_resets_" + i, this.triggeredBlocks.get(key).getRight());
        }

        nbt.putInt("counter", this.counter);

        super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.triggeredBlocks.clear();
        int triggeredBlocksKeysSize = nbt.getInt("triggeredBlocksKeysSize");
        for (int i = 0; i < triggeredBlocksKeysSize; i++) {
            this.triggeredBlocks.put(nbt.getInt("triggeredBlocks_key_" + i), new MutablePair<>(
                    new BlockPos(
                            MathHelper.clamp(nbt.getInt("triggeredBlocks_entry_X_" + i), -48, 48),
                            MathHelper.clamp(nbt.getInt("triggeredBlocks_entry_Y_" + i), -48, 48),
                            MathHelper.clamp(nbt.getInt("triggeredBlocks_entry_Z_" + i), -48, 48)
                    ), nbt.getBoolean("triggeredBlocks_entry_resets_" + i)));
        }

        this.counter = nbt.getInt("counter");

        super.readNbt(nbt);
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return this.createNbt();
    }

    public HashMap<Integer, MutablePair<BlockPos, Boolean>> getTriggeredBlocks() {
        return triggeredBlocks;
    }

    public boolean setTriggeredBlocks(HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks) {
        this.triggeredBlocks = triggeredBlocks;
        return true;
    }

    public void trigger() {
        if (this.world != null) {
            this.counter++;
            if (this.triggeredBlocks.containsKey(this.counter)) {
                MutablePair<BlockPos, Boolean> triggeredBlock = this.triggeredBlocks.get(this.counter);
                BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + triggeredBlock.getLeft().getX(), this.pos.getY() + triggeredBlock.getLeft().getY(), this.pos.getZ() + triggeredBlock.getLeft().getZ()));
                if (blockEntity != this) {
                    boolean triggeredBlockResets = triggeredBlock.getRight();
                    if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
                        resetable.reset();
                    } else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
                        triggerable.trigger();
                    }
                }
            }
        }
    }

    @Override
    public void reset() {
        this.counter = 0;
    }

    @Override
    protected void onRotate(BlockState state) {
        if (state.getBlock() instanceof RotatedBlockWithEntity) {
            if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
                BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
                List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
                for (Integer key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
                    this.triggeredBlocks.put(key, oldBlockPos);
                }
                this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
            }
            if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
                List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
                for (Integer key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
                    this.triggeredBlocks.put(key, oldBlockPos);
                }
                this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
            }
            if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
                List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
                for (Integer key : keys) {
                    MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
                    oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
                    this.triggeredBlocks.put(key, oldBlockPos);
                }
                this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
            }
        }
    }
}
