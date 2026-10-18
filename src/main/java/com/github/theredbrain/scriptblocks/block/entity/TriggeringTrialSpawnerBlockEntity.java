package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
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
import net.minecraft.nbt.NbtHelper;
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
		super(ScriptBlocksEntities.TRIGGERING_TRIAL_SPAWNER, pos, state);
		EntityDetector entityDetector = EntityDetector.SURVIVAL_PLAYERS;
		EntityDetector.Selector selector = EntityDetector.Selector.IN_WORLD;
		this.spawner = new TrialSpawnerLogic(this, entityDetector, selector);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnInactive.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnInactive.getRight());
		nbt.put("on_inactive", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForPlayers.getRight());
		nbt.put("on_waiting_for_players", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnActive.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnActive.getRight());
		nbt.put("on_active", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnWaitingForReward.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForReward.getRight());
		nbt.put("on_waiting_for_reward", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnReward.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnReward.getRight());
		nbt.put("on_reward", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", NbtHelper.fromBlockPos(this.triggeredBlockOnCooldown.getLeft()));
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnCooldown.getRight());
		nbt.put("on_cooldown", nbtCompound);

		this.spawner
				.codec()
				.encodeStart(NbtOps.INSTANCE, this.spawner)
				.ifSuccess(nbtx -> nbt.copyFrom((NbtCompound) nbtx))
				.ifError(error -> LOGGER.warn("Failed to encode TrialSpawner {}", error.message()));

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("triggeredBlockOnInactivePositionOffsetX") || nbt.contains("triggeredBlockOnInactivePositionOffsetY") || nbt.contains("triggeredBlockOnInactivePositionOffsetZ") || nbt.contains("triggeredBlockOnInactiveResets")) {
			this.triggeredBlockOnInactive = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnInactivePositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnInactiveResets")
			);
			nbt.remove("triggeredBlockOnInactivePositionOffsetX");
			nbt.remove("triggeredBlockOnInactivePositionOffsetY");
			nbt.remove("triggeredBlockOnInactivePositionOffsetZ");
			nbt.remove("triggeredBlockOnInactiveResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_inactive");
			this.triggeredBlockOnInactive = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("triggeredBlockOnWaitingForPlayersPositionOffsetX") || nbt.contains("triggeredBlockOnWaitingForPlayersPositionOffsetY") || nbt.contains("triggeredBlockOnWaitingForPlayersPositionOffsetZ") || nbt.contains("triggeredBlockOnWaitingForPlayersResets")) {
			this.triggeredBlockOnWaitingForPlayers = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForPlayersPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnWaitingForPlayersResets")
			);
			nbt.remove("triggeredBlockOnWaitingForPlayersPositionOffsetX");
			nbt.remove("triggeredBlockOnWaitingForPlayersPositionOffsetY");
			nbt.remove("triggeredBlockOnWaitingForPlayersPositionOffsetZ");
			nbt.remove("triggeredBlockOnWaitingForPlayersResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_waiting_for_players");
			this.triggeredBlockOnWaitingForPlayers = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("triggeredBlockOnActivePositionOffsetX") || nbt.contains("triggeredBlockOnActivePositionOffsetY") || nbt.contains("triggeredBlockOnActivePositionOffsetZ") || nbt.contains("triggeredBlockOnActiveResets")) {
			this.triggeredBlockOnActive = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnActivePositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnActiveResets")
			);
			nbt.remove("triggeredBlockOnActivePositionOffsetX");
			nbt.remove("triggeredBlockOnActivePositionOffsetY");
			nbt.remove("triggeredBlockOnActivePositionOffsetZ");
			nbt.remove("triggeredBlockOnActiveResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_active");
			this.triggeredBlockOnActive = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("triggeredBlockOnWaitingForRewardPositionOffsetX") || nbt.contains("triggeredBlockOnWaitingForRewardPositionOffsetY") || nbt.contains("triggeredBlockOnWaitingForRewardPositionOffsetZ") || nbt.contains("triggeredBlockOnWaitingForRewardResets")) {
			this.triggeredBlockOnWaitingForReward = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnWaitingForRewardPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnWaitingForRewardResets")
			);
			nbt.remove("triggeredBlockOnWaitingForRewardPositionOffsetX");
			nbt.remove("triggeredBlockOnWaitingForRewardPositionOffsetY");
			nbt.remove("triggeredBlockOnWaitingForRewardPositionOffsetZ");
			nbt.remove("triggeredBlockOnWaitingForRewardResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_waiting_for_reward");
			this.triggeredBlockOnWaitingForReward = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("triggeredBlockOnRewardPositionOffsetX") || nbt.contains("triggeredBlockOnRewardPositionOffsetY") || nbt.contains("triggeredBlockOnRewardPositionOffsetZ") || nbt.contains("triggeredBlockOnRewardResets")) {
			this.triggeredBlockOnReward = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnRewardPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnRewardResets")
			);
			nbt.remove("triggeredBlockOnRewardPositionOffsetX");
			nbt.remove("triggeredBlockOnRewardPositionOffsetY");
			nbt.remove("triggeredBlockOnRewardPositionOffsetZ");
			nbt.remove("triggeredBlockOnRewardResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_reward");
			this.triggeredBlockOnReward = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("triggeredBlockOnCooldownPositionOffsetX") || nbt.contains("triggeredBlockOnCooldownPositionOffsetY") || nbt.contains("triggeredBlockOnCooldownPositionOffsetZ") || nbt.contains("triggeredBlockOnCooldownResets")) {
			this.triggeredBlockOnCooldown = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockOnCooldownPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockOnCooldownResets")
			);
			nbt.remove("triggeredBlockOnCooldownPositionOffsetX");
			nbt.remove("triggeredBlockOnCooldownPositionOffsetY");
			nbt.remove("triggeredBlockOnCooldownPositionOffsetZ");
			nbt.remove("triggeredBlockOnCooldownResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("on_cooldown");
			this.triggeredBlockOnCooldown = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

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
		switch (spawnerState) {
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), blockRotation));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), blockRotation));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), blockRotation));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), blockRotation));
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), BlockMirror.FRONT_BACK));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), BlockMirror.LEFT_RIGHT));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
