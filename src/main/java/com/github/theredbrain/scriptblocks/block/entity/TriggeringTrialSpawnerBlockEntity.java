package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

public class TriggeringTrialSpawnerBlockEntity extends RotatedBlockEntity implements Spawner, TrialSpawner.StateAccessor {
	private MutablePair<BlockPos, Boolean> triggeredBlockOnInactive = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForPlayers = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnActive = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForReward = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnReward = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnCooldown = new MutablePair<>(BlockPos.ZERO, false);

	private final TrialSpawner logic = this.createDefaultLogic();

	public TriggeringTrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERING_TRIAL_SPAWNER, pos, state);
	}

	private TrialSpawner createDefaultLogic() {
		PlayerDetector entityDetector = SharedConstants.DEBUG_TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS ? PlayerDetector.SHEEP : PlayerDetector.NO_CREATIVE_PLAYERS;
		PlayerDetector.EntitySelector selector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
		return new TrialSpawner(TrialSpawner.FullConfig.DEFAULT, this, entityDetector, selector);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnInactive.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnInactive.getRight());
		view.store("on_inactive", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnWaitingForPlayers.getLeft())
		;
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForPlayers.getRight());
		view.store("on_waiting_for_players", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnActive.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnActive.getRight());
		view.store("on_active", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnWaitingForReward.getLeft())
		;
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForReward.getRight());
		view.store("on_waiting_for_reward", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnReward.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnReward.getRight());
		view.store("on_reward", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnCooldown.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnCooldown.getRight());
		view.store("on_cooldown", CompoundTag.CODEC, nbtCompound.copy());

		this.logic.store(view);

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("on_inactive", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnInactive = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		nbtCompound = view.read("on_waiting_for_players", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnWaitingForPlayers = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		nbtCompound = view.read("on_active", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnActive = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		nbtCompound = view.read("on_waiting_for_reward", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnWaitingForReward = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		nbtCompound = view.read("on_reward", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnReward = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		nbtCompound = view.read("on_cooldown", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlockOnCooldown = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.logic.load(view);
		if (this.level != null) {
			this.markUpdated();
		}

		super.loadAdditional(view);

	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.logic.getStateData().getUpdateTag(this.getBlockState().getValue(TrialSpawnerBlock.STATE));
	}

	@Override
	public void setEntityId(EntityType<?> type, RandomSource random) {
		if (this.level == null) {
			Util.logAndPauseIfInIde("Expected non-null level");
		} else {
			this.logic.overrideEntityToSpawn(type, this.level);
			this.setChanged();
		}
	}

	public TrialSpawner getSpawner() {
		return this.logic;
	}

	@Override
	public TrialSpawnerState getState() {
		return !this.getBlockState().hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE)
				? TrialSpawnerState.INACTIVE
				: this.getBlockState().getValue(BlockStateProperties.TRIAL_SPAWNER_STATE);
	}

	@Override
	public void setState(Level world, TrialSpawnerState spawnerState) {
		switch (spawnerState) {
			case TrialSpawnerState.INACTIVE -> this.triggerOnInactive();
			case TrialSpawnerState.WAITING_FOR_PLAYERS -> this.triggerOnWaitingForPlayers();
			case TrialSpawnerState.ACTIVE -> this.triggerOnActive();
			case TrialSpawnerState.WAITING_FOR_REWARD_EJECTION -> this.triggerOnWaitingForReward();
			case TrialSpawnerState.EJECTING_REWARD -> this.triggerOnReward();
			default -> this.triggerOnCooldown();
		}
		this.setChanged();
		world.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, spawnerState));
	}

	@Override
	public void markUpdated() {
		this.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		}
	}

	private void triggerOnInactive() {
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnInactive.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnInactive.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnInactive.getLeft().getZ()));
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
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnWaitingForPlayers.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnWaitingForPlayers.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnWaitingForPlayers.getLeft().getZ()));
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
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnActive.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnActive.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnActive.getLeft().getZ()));
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
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnWaitingForReward.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnWaitingForReward.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnWaitingForReward.getLeft().getZ()));
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
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnReward.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnReward.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnReward.getLeft().getZ()));
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
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlockOnCooldown.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlockOnCooldown.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlockOnCooldown.getLeft().getZ()));
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
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), blockRotation));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), blockRotation));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), blockRotation));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), blockRotation));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), blockRotation));
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), Mirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), Mirror.FRONT_BACK));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), Mirror.FRONT_BACK));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), Mirror.FRONT_BACK));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), Mirror.FRONT_BACK));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), Mirror.FRONT_BACK));
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.triggeredBlockOnInactive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnInactive.getLeft(), Mirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForPlayers.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForPlayers.getLeft(), Mirror.LEFT_RIGHT));
				this.triggeredBlockOnActive.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnActive.getLeft(), Mirror.LEFT_RIGHT));
				this.triggeredBlockOnWaitingForReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnWaitingForReward.getLeft(), Mirror.LEFT_RIGHT));
				this.triggeredBlockOnReward.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnReward.getLeft(), Mirror.LEFT_RIGHT));
				this.triggeredBlockOnCooldown.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlockOnCooldown.getLeft(), Mirror.LEFT_RIGHT));
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
