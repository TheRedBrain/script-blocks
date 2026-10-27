package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SpawnPointDelegationBlockEntity extends RotatedBlockEntity {
	private final List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints = new ArrayList<>(List.of());

	public SpawnPointDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.SPAWN_POINT_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("delegated_spawn_points", CompoundTag.CODEC);
		for (MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint : this.delegatedSpawnPoints) {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, spawnPoint.getLeft());
			nbtCompound.putDouble("yaw", spawnPoint.getRight().getLeft());
			nbtCompound.putDouble("pitch", spawnPoint.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.delegatedSpawnPoints.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("delegated_spawn_points", CompoundTag.CODEC)) {
			this.delegatedSpawnPoints.add(new MutablePair<>(
					nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
					new MutablePair<>(
							Mth.clamp(nbtCompound.getDoubleOr("yaw", 0), -180, 180),
							Mth.clamp(nbtCompound.getDoubleOr("pitch", 0), -90, 90)
					)
			));
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
	public List<MutablePair<BlockPos, MutablePair<Double, Double>>> getDelegatedSpawnPoints() {
		return this.delegatedSpawnPoints;
	}

	public void setDelegatedSpawnPoints(List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints) {
		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(delegatedSpawnPoints);
	}
	// endregion --- getter & setter ---

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetSpawnPoint(ServerLevel serverWorld) {
		BlockPos positionOffset = new BlockPos(0, 1, 0);
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation = new MutablePair<>(0.0, 0.0);
		int index = 0;

		if (this.getLevel() != null && !this.delegatedSpawnPoints.isEmpty()) {
			index = this.getLevel().getRandom().nextInt(this.delegatedSpawnPoints.size());
		}

		if (index < this.delegatedSpawnPoints.size()) {
			positionOffset = this.delegatedSpawnPoints.get(index).getLeft();
			targetOrientation = this.delegatedSpawnPoints.get(index).getRight();
		}

		targetPos = new BlockPos(positionOffset.getX() + this.getBlockPos().getX(), positionOffset.getY() + this.getBlockPos().getY(), positionOffset.getZ() + this.getBlockPos().getZ());

		if (!positionOffset.equals(BlockPos.ZERO) && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.rotateSpawnPoint(delegatedSpawnPoint, blockRotation));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, Mirror.FRONT_BACK));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, Mirror.LEFT_RIGHT));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
