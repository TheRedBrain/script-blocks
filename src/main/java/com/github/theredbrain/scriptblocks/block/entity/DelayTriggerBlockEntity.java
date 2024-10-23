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
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class DelayTriggerBlockEntity extends RotatedBlockEntity implements Triggerable {
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private int triggerDelay = 0;
	private int remainingTicks = 0;

	public DelayTriggerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DELAY_TRIGGER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.triggeredBlock.getLeft().getX() != 0 || this.triggeredBlock.getLeft().getY() != 0 || this.triggeredBlock.getLeft().getZ() != 0 || this.triggeredBlock.getRight()) {
			nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
			nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
			nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
			nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());
		} else {
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		}

		if (this.triggerDelay > 0) {
			nbt.putInt("triggerDelay", this.triggerDelay);
		} else {
			nbt.remove("triggerDelay");
		}

		if (this.remainingTicks > 0) {
			nbt.putInt("remainingTicks", this.remainingTicks);
		} else {
			nbt.remove("remainingTicks");
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("triggeredBlockPositionOffsetX") && nbt.contains("triggeredBlockPositionOffsetY") && nbt.contains("triggeredBlockPositionOffsetZ") && nbt.contains("triggeredBlockResets")) {
			int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
			int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
			int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
			this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));
		} else {
			this.triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
		}

		if (nbt.contains("triggerDelay")) {
			this.triggerDelay = Math.max(0, nbt.getInt("triggerDelay"));
		} else {
			this.triggerDelay = 0;
		}

		if (nbt.contains("remainingTicks")) {
			this.remainingTicks = Math.max(0, nbt.getInt("remainingTicks"));
		} else {
			this.remainingTicks = 0;
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, DelayTriggerBlockEntity blockEntity) {

		if (!world.isClient && blockEntity.remainingTicks > 0) {
			blockEntity.remainingTicks--;
			if (blockEntity.remainingTicks <= 0) {
				blockEntity.triggerTriggeredBlock();
			}
		}
	}

	public void trigger() {
		if (this.world != null) {
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

	public void setTriggerDelay(int triggerDelay) {
		this.triggerDelay = Math.max(0, triggerDelay);
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
