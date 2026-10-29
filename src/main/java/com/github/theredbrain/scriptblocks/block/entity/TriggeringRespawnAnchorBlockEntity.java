package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TriggeringRespawnAnchorBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private LevelData.RespawnData spawnPoint = LevelData.RespawnData.DEFAULT;
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private final Set<UUID> uuidList = new HashSet<>();
	private boolean sendMessage = true;

	public TriggeringRespawnAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public TriggeringRespawnAnchorBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.TRIGGERING_RESPAWN_ANCHOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("spawn_point", LevelData.RespawnData.CODEC, this.spawnPoint);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<UUID> listAppender = view.list("uuid_list", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.uuidList) {
			listAppender.add(uuid);
		}

		view.putBoolean("send_message", this.sendMessage);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.spawnPoint = view.read("spawn_point", LevelData.RespawnData.CODEC).orElseGet(() -> {
					CompoundTag nbtCompound = view.read("spawn_point", CompoundTag.CODEC).orElseGet(CompoundTag::new);
					return LevelData.RespawnData.of(
							this.getLevel() != null ? this.getLevel().dimension() : ServerLevel.OVERWORLD,
							nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							nbtCompound.getFloatOr("yaw", 0),
							nbtCompound.getFloatOr("pitch", 0)
					);
				}
		);

		CompoundTag nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.triggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("resets", false)
		);

		this.uuidList.clear();
		for (UUID uuid : view.listOrEmpty("uuid_list", UUIDUtil.AUTHLIB_CODEC)) {
			this.uuidList.add(uuid);
		}

		this.sendMessage = view.getBooleanOr("send_message", true);

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
	public LevelData.RespawnData getSpawnPoint() {
		return this.spawnPoint;
	}

	public void setSpawnPoint(LevelData.RespawnData spawnPoint) {
		this.spawnPoint = spawnPoint;
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public boolean sendMessage() {
		return this.sendMessage;
	}

	public void setSendMessage(boolean sendMessage) {
		this.sendMessage = sendMessage;
	}
	// endregion --- getter & setter ---

	public LevelData.RespawnData getActualSpawnPoint(Level level) {
		LevelData.RespawnData spawnPoint = this.spawnPoint;

		if (!this.spawnPoint.pos().equals(BlockPos.ZERO) && level.getBlockEntity(BlockRotationUtils.getActualOffsetBlockPosition(this.getBlockPos(), this.spawnPoint.pos())) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			LevelData.RespawnData delegatedSpawnpoint = spawnPointDelegationBlockEntity.getTargetSpawnPoint(level);
			if (delegatedSpawnpoint != null) {
				spawnPoint = delegatedSpawnpoint;
			}
		}

		return spawnPoint;
	}

	public void trigger() {
		if (this.level != null) {
			BlockEntity blockEntity = level.getBlockEntity(new BlockPos(this.worldPosition.getX() + this.triggeredBlock.getLeft().getX(), this.worldPosition.getY() + this.triggeredBlock.getLeft().getY(), this.worldPosition.getZ() + this.triggeredBlock.getLeft().getZ()));
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

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		Iterator<UUID> listIterator = list.iterator();
		Player playerEntity;
		UUID uuid;
		if (this.getLevel() != null) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = this.getLevel().getPlayerByUUID(uuid);

				if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
					serverPlayerEntity.setRespawnPosition(remove ? null : new ServerPlayer.RespawnConfig(LevelData.RespawnData.of(this.getLevel().dimension(), this.worldPosition, 0.0F, 0.0F), false), !remove && this.sendMessage);
				}
			}
		}

		if (remove) {
			list.forEach(this.uuidList::remove);
		} else {
			this.uuidList.addAll(list);
		}

		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void reset() {

		Iterator<UUID> listIterator = this.uuidList.iterator();
		Player playerEntity;
		UUID uuid;
		if (this.getLevel() instanceof ServerLevel serverLevel) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = serverLevel.getPlayerByUUID(uuid);

				if (playerEntity instanceof ServerPlayer serverPlayerEntity) {
					serverPlayerEntity.setRespawnPosition(new ServerPlayer.RespawnConfig(this.getActualSpawnPoint(serverLevel), false), false);
				}
			}
		}

		this.uuidList.clear();
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
				this.spawnPoint = BlockRotationUtils.rotateSpawnPoint(this.spawnPoint, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, Mirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, Mirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
