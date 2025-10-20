package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.Spawner;
import net.minecraft.block.enums.TrialSpawnerState;
import net.minecraft.block.spawner.EntityDetector;
import net.minecraft.block.spawner.TrialSpawnerLogic;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;

public class TriggeringTrialSpawnerBlockEntity extends RotatedBlockEntity implements Spawner, TrialSpawnerLogic.TrialSpawner {
	private MutablePair<BlockPos, Boolean> triggeredBlockOnInactive = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForPlayers = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnActive = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForReward = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnReward = new MutablePair<>(new BlockPos(0, 0, 0), false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnCooldown = new MutablePair<>(new BlockPos(0, 0, 0), false);
	
	private static final Logger LOGGER = LogUtils.getLogger();
	private TrialSpawnerLogic spawner;

	public TriggeringTrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.TRIGGERING_TRIAL_SPAWNER, pos, state);
		EntityDetector entityDetector = EntityDetector.SURVIVAL_PLAYERS;
		EntityDetector.Selector selector = EntityDetector.Selector.IN_WORLD;
		this.spawner = new TrialSpawnerLogic(this, entityDetector, selector);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetZ"), -48, 48);
		this.triggeredBlockOnInactive = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnInactiveResets"));

		x = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetZ"), -48, 48);
		this.triggeredBlockOnWaitingForPlayers = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnWaitingForPlayersResets"));

		x = MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetZ"), -48, 48);
		this.triggeredBlockOnActive = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnActiveResets"));

		x = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetZ"), -48, 48);
		this.triggeredBlockOnWaitingForReward = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnWaitingForRewardResets"));

		x = MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetZ"), -48, 48);
		this.triggeredBlockOnReward = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnRewardResets"));

		x = MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetX"), -48, 48);
		y = MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetY"), -48, 48);
		z = MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetZ"), -48, 48);
		this.triggeredBlockOnCooldown = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockOnCooldownResets"));

		if (nbt.contains("normal_config")) {
			NbtCompound nbtCompound = nbt.getCompound("normal_config").copy();
			nbt.put("ominous_config", nbtCompound.copyFrom(nbt.getCompound("ominous_config")));
		}

		this.spawner.codec().parse(NbtOps.INSTANCE, nbt).resultOrPartial(LOGGER::error).ifPresent(spawner -> this.spawner = spawner);
		if (this.world != null) {
			this.updateListeners();
		}

		super.readNbt(nbt, registryLookup);

	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("triggeredBlockOnInactivePositionOffsetX", this.triggeredBlockOnInactive.getLeft().getX());
		nbt.putInt("triggeredBlockOnInactivePositionOffsetY", this.triggeredBlockOnInactive.getLeft().getY());
		nbt.putInt("triggeredBlockOnInactivePositionOffsetZ", this.triggeredBlockOnInactive.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnInactiveResets", this.triggeredBlockOnInactive.getRight());

		nbt.putInt("triggeredBlockOnWaitingForPlayersPositionOffsetX", this.triggeredBlockOnWaitingForPlayers.getLeft().getX());
		nbt.putInt("triggeredBlockOnWaitingForPlayersPositionOffsetY", this.triggeredBlockOnWaitingForPlayers.getLeft().getY());
		nbt.putInt("triggeredBlockOnWaitingForPlayersPositionOffsetZ", this.triggeredBlockOnWaitingForPlayers.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnWaitingForPlayersResets", this.triggeredBlockOnWaitingForPlayers.getRight());

		nbt.putInt("triggeredBlockOnActivePositionOffsetX", this.triggeredBlockOnActive.getLeft().getX());
		nbt.putInt("triggeredBlockOnActivePositionOffsetY", this.triggeredBlockOnActive.getLeft().getY());
		nbt.putInt("triggeredBlockOnActivePositionOffsetZ", this.triggeredBlockOnActive.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnActiveResets", this.triggeredBlockOnActive.getRight());

		nbt.putInt("triggeredBlockOnWaitingForRewardPositionOffsetX", this.triggeredBlockOnWaitingForReward.getLeft().getX());
		nbt.putInt("triggeredBlockOnWaitingForRewardPositionOffsetY", this.triggeredBlockOnWaitingForReward.getLeft().getY());
		nbt.putInt("triggeredBlockOnWaitingForRewardPositionOffsetZ", this.triggeredBlockOnWaitingForReward.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnWaitingForRewardResets", this.triggeredBlockOnWaitingForReward.getRight());

		nbt.putInt("triggeredBlockOnRewardPositionOffsetX", this.triggeredBlockOnReward.getLeft().getX());
		nbt.putInt("triggeredBlockOnRewardPositionOffsetY", this.triggeredBlockOnReward.getLeft().getY());
		nbt.putInt("triggeredBlockOnRewardPositionOffsetZ", this.triggeredBlockOnReward.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnRewardResets", this.triggeredBlockOnReward.getRight());

		nbt.putInt("triggeredBlockOnCooldownPositionOffsetX", this.triggeredBlockOnCooldown.getLeft().getX());
		nbt.putInt("triggeredBlockOnCooldownPositionOffsetY", this.triggeredBlockOnCooldown.getLeft().getY());
		nbt.putInt("triggeredBlockOnCooldownPositionOffsetZ", this.triggeredBlockOnCooldown.getLeft().getZ());
		nbt.putBoolean("triggeredBlockOnCooldownResets", this.triggeredBlockOnCooldown.getRight());

		this.spawner
				.codec()
				.encodeStart(NbtOps.INSTANCE, this.spawner)
				.ifSuccess(nbtx -> nbt.copyFrom((NbtCompound)nbtx))
				.ifError(error -> LOGGER.warn("Failed to encode TrialSpawner {}", error.message()));

		super.writeNbt(nbt, registryLookup);

	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.spawner.getData().getSpawnDataNbt(this.getCachedState().get(TrialSpawnerBlock.TRIAL_SPAWNER_STATE));
	}

	@Override
	public boolean copyItemDataRequiresOperator() {
		return true;
	}

	@Override
	public void setEntityType(EntityType<?> type, Random random) {
		this.spawner.getData().setEntityType(this.spawner, random, type);
		this.markDirty();
	}

	public TrialSpawnerLogic getSpawner() {
		return this.spawner;
	}

	@Override
	public TrialSpawnerState getSpawnerState() {
		return !this.getCachedState().contains(Properties.TRIAL_SPAWNER_STATE)
				? TrialSpawnerState.INACTIVE
				: this.getCachedState().get(Properties.TRIAL_SPAWNER_STATE);
	}

	@Override
	public void setSpawnerState(World world, TrialSpawnerState spawnerState) {
		switch(spawnerState) {
			case TrialSpawnerState.INACTIVE -> this.triggerOnInactive();
			case TrialSpawnerState.WAITING_FOR_PLAYERS -> this.triggerOnWaitingForPlayers();
			case TrialSpawnerState.ACTIVE -> this.triggerOnActive();
			case TrialSpawnerState.WAITING_FOR_REWARD_EJECTION -> this.triggerOnWaitingForReward();
			case TrialSpawnerState.EJECTING_REWARD -> this.triggerOnReward();
			default -> this.triggerOnCooldown();
		}
		this.markDirty();
		world.setBlockState(this.pos, this.getCachedState().with(Properties.TRIAL_SPAWNER_STATE, spawnerState));
	}

	@Override
	public void updateListeners() {
		this.markDirty();
		if (this.world != null) {
			this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_ALL);
		}
	}

	private void triggerOnInactive() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnInactive.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnInactive.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnInactive.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnInactive.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	private void triggerOnWaitingForPlayers() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnWaitingForPlayers.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnWaitingForPlayers.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnWaitingForPlayers.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnWaitingForPlayers.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	private void triggerOnActive() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnActive.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnActive.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnActive.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnActive.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	private void triggerOnWaitingForReward() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnWaitingForReward.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnWaitingForReward.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnWaitingForReward.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnWaitingForReward.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	private void triggerOnReward() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnReward.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnReward.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnReward.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnReward.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	private void triggerOnCooldown() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlockOnCooldown.getLeft().getX(), this.pos.getY() + this.triggeredBlockOnCooldown.getLeft().getY(), this.pos.getZ() + this.triggeredBlockOnCooldown.getLeft().getZ()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.triggeredBlockOnCooldown.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}
	
	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), blockRotation));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), blockRotation));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), blockRotation));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), blockRotation));
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
