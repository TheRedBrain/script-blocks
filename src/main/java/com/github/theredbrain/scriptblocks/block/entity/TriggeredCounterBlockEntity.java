package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TriggeredCounterBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
	private final HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
	private int counter = 0;

	public TriggeredCounterBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_COUNTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("triggered_blocks", CompoundTag.CODEC);
		this.triggeredBlocks.forEach((key, entry) -> {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putInt("key", key);
			nbtCompound.store("position_offset", BlockPos.CODEC, entry.getLeft());
			nbtCompound.putBoolean("resets", entry.getRight());
			listAppender.add(nbtCompound);
		});

		view.putInt("counter", this.counter);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.triggeredBlocks.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("triggered_blocks", CompoundTag.CODEC)) {
			this.triggeredBlocks.put(
					nbtCompound.getIntOr("key", 0),
					new MutablePair<>(
							nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							nbtCompound.getBooleanOr("resets", false)
					)
			);
		}

		this.counter = view.getIntOr("counter", 0);

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	// region --- getter & setter ---
	public HashMap<Integer, MutablePair<BlockPos, Boolean>> getTriggeredBlocks() {
		return triggeredBlocks;
	}

	public boolean setTriggeredBlocks(HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks) {
		this.triggeredBlocks.clear();
		this.triggeredBlocks.putAll(triggeredBlocks);
		return true;
	}
	// endregion --- getter & setter ---

	public void trigger() {
		if (this.level != null) {
			this.counter++;
			if (this.triggeredBlocks.containsKey(this.counter)) {
				MutablePair<BlockPos, Boolean> triggeredBlock = this.triggeredBlocks.get(this.counter);
				BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + triggeredBlock.getLeft().getX(), this.worldPosition.getY() + triggeredBlock.getLeft().getY(), this.worldPosition.getZ() + triggeredBlock.getLeft().getZ()));
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
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void reset() {
		this.counter = 0;
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.FRONT_BACK));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), Mirror.LEFT_RIGHT));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
