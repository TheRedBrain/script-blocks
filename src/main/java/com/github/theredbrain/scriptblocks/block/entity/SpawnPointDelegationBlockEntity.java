package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpawnPointDelegationBlockEntity extends RotatedBlockEntity {
	private final List<LevelData.RespawnData> delegatedSpawnPoints = new ArrayList<>(List.of());

	public SpawnPointDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.SPAWN_POINT_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<LevelData.RespawnData> listAppender = view.list("delegated_spawn_points", LevelData.RespawnData.CODEC);
		for (LevelData.RespawnData spawnPoint : this.delegatedSpawnPoints) {
			listAppender.add(spawnPoint);
		}

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.delegatedSpawnPoints.clear();
		ValueInput.TypedInputList<LevelData.RespawnData> list = view.listOrEmpty("delegated_spawn_points", LevelData.RespawnData.CODEC);
		if (!list.isEmpty()) {
			for (LevelData.RespawnData spawnPoint : list) {
				this.delegatedSpawnPoints.add(spawnPoint);
			}
		} else if (this.getLevel() != null) {
			for (CompoundTag nbtCompound : view.listOrEmpty("delegated_spawn_points", CompoundTag.CODEC)) {
				this.delegatedSpawnPoints.add(LevelData.RespawnData.of(
						this.getLevel().dimension(),
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						nbtCompound.getFloatOr("yaw", 0),
						nbtCompound.getFloatOr("pitch", 0)
				));
			}
		}

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
	public List<LevelData.RespawnData> getDelegatedSpawnPoints() {
		return this.delegatedSpawnPoints;
	}

	public void setDelegatedSpawnPoints(List<LevelData.RespawnData> delegatedSpawnPoints) {
		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(delegatedSpawnPoints);
	}
	// endregion --- getter & setter ---

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	@Nullable
	public LevelData.RespawnData getTargetSpawnPoint(Level serverWorld) {
		int index = 0;

		if (!this.delegatedSpawnPoints.isEmpty()) {
			index = serverWorld.getRandom().nextInt(this.delegatedSpawnPoints.size());
		}
		LevelData.RespawnData delegatedSpawnpoint = null;

		if (index < this.delegatedSpawnPoints.size()) {
			delegatedSpawnpoint = this.delegatedSpawnPoints.get(index);
		}
		if (delegatedSpawnpoint != null && !delegatedSpawnpoint.pos().equals(BlockPos.ZERO)) {

			if (serverWorld.getBlockEntity(getActualOffsetBlockPosition(this.getBlockPos(), delegatedSpawnpoint.pos())) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
				return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
			}
		}

		return null;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<LevelData.RespawnData> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (LevelData.RespawnData delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.rotateSpawnPoint(delegatedSpawnPoint, blockRotation));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<LevelData.RespawnData> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (LevelData.RespawnData delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, Mirror.FRONT_BACK));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<LevelData.RespawnData> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (LevelData.RespawnData delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, Mirror.LEFT_RIGHT));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
