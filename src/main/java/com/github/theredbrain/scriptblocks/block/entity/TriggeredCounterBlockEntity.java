package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TriggeredCounterBlockEntity extends RotatedBlockEntity implements Triggerable, Resetable {
	private final HashMap<Integer, MutablePair<BlockPos, Boolean>> triggeredBlocks = new HashMap<>();
	private int counter = 0;

	public TriggeredCounterBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_COUNTER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("triggered_blocks", NbtCompound.CODEC);
		this.triggeredBlocks.forEach((key, entry) -> {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putInt("key", key);
			nbtCompound.put("position_offset", BlockPos.CODEC, entry.getLeft());
			nbtCompound.putBoolean("resets", entry.getRight());
			listAppender.add(nbtCompound);
		});

		view.putInt("counter", this.counter);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.triggeredBlocks.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("triggered_blocks", NbtCompound.CODEC)) {
			this.triggeredBlocks.put(
					nbtCompound.getInt("key", 0),
					new MutablePair<>(
							nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							nbtCompound.getBoolean("resets", false)
					)
			);
		}

		this.counter = view.getInt("counter", 0);

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	public void reset() {
		this.counter = 0;
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.rotateOffsetBlockPos(oldBlockPos.getLeft(), blockRotation));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.FRONT_BACK));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				List<Integer> keys = new ArrayList<>(this.triggeredBlocks.keySet());
				for (Integer key : keys) {
					MutablePair<BlockPos, Boolean> oldBlockPos = this.triggeredBlocks.get(key);
					oldBlockPos.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(oldBlockPos.getLeft(), BlockMirror.LEFT_RIGHT));
					this.triggeredBlocks.put(key, oldBlockPos);
				}
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
