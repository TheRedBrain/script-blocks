package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.SharedConstants;
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
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

public class TriggeringTrialSpawnerBlockEntity extends RotatedBlockEntity implements Spawner, TrialSpawnerLogic.TrialSpawner {
	private MutablePair<BlockPos, Boolean> triggeredBlockOnInactive = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForPlayers = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnActive = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnWaitingForReward = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnReward = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> triggeredBlockOnCooldown = new MutablePair<>(BlockPos.ORIGIN, false);

	private final TrialSpawnerLogic logic = this.createDefaultLogic();

	public TriggeringTrialSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERING_TRIAL_SPAWNER, pos, state);
	}

	private TrialSpawnerLogic createDefaultLogic() {
		EntityDetector entityDetector = SharedConstants.TRIAL_SPAWNER_DETECTS_SHEEP_AS_PLAYERS ? EntityDetector.SHEEP : EntityDetector.SURVIVAL_PLAYERS;
		EntityDetector.Selector selector = EntityDetector.Selector.IN_WORLD;
		return new TrialSpawnerLogic(TrialSpawnerLogic.FullConfig.DEFAULT, this, entityDetector, selector);
	}

	@Override
	protected void writeData(WriteView view) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnInactive.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnInactive.getRight());
		view.put("on_inactive", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnWaitingForPlayers.getLeft())
		;
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForPlayers.getRight());
		view.put("on_waiting_for_players", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnActive.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnActive.getRight());
		view.put("on_active", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnWaitingForReward.getLeft())
		;
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnWaitingForReward.getRight());
		view.put("on_waiting_for_reward", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnReward.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnReward.getRight());
		view.put("on_reward", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.triggeredBlockOnCooldown.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.triggeredBlockOnCooldown.getRight());
		view.put("on_cooldown", NbtCompound.CODEC, nbtCompound.copy());

		this.logic.writeData(view);

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		NbtCompound nbtCompound = view.read("on_inactive", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnInactive = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		nbtCompound = view.read("on_waiting_for_players", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnWaitingForPlayers = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		nbtCompound = view.read("on_active", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnActive = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		nbtCompound = view.read("on_waiting_for_reward", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnWaitingForReward = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		nbtCompound = view.read("on_reward", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnReward = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		nbtCompound = view.read("on_cooldown", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlockOnCooldown = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		this.logic.readData(view);
		if (this.world != null) {
			this.updateListeners();
		}

		super.readData(view);

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
		if (this.world == null) {
			Util.logErrorOrPause("Expected non-null level");
		} else {
			this.logic.setEntityType(type, this.world);
			this.markDirty();
		}
	}

	public TrialSpawnerLogic getSpawner() {
		return this.logic;
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
