package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class SpawnPointDelegationBlockEntity extends RotatedBlockEntity {
	private final List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints = new ArrayList<>(List.of());

	public SpawnPointDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.SPAWN_POINT_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("delegated_spawn_points_size", delegatedSpawnPoints.size());
		for (int i = 0; i < this.delegatedSpawnPoints.size(); i++) {
			nbt.putInt("delegated_spawn_point_x_" + i, this.delegatedSpawnPoints.get(i).getLeft().getX());
			nbt.putInt("delegated_spawn_point_y_" + i, this.delegatedSpawnPoints.get(i).getLeft().getY());
			nbt.putInt("delegated_spawn_point_z_" + i, this.delegatedSpawnPoints.get(i).getLeft().getZ());
			nbt.putDouble("delegated_spawn_point_yaw_" + i, this.delegatedSpawnPoints.get(i).getRight().getLeft());
			nbt.putDouble("delegated_spawn_point_pitch_" + i, this.delegatedSpawnPoints.get(i).getRight().getRight());
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.delegatedSpawnPoints.clear();

		int delegatedSpawnBlocksSize = nbt.getInt("delegated_spawn_points_size");
		for (int i = 0; i < delegatedSpawnBlocksSize; i++) {
			this.delegatedSpawnPoints.add(new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("delegated_spawn_point_x_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("delegated_spawn_point_y_" + i), -48, 48),
							MathHelper.clamp(nbt.getInt("delegated_spawn_point_z_" + i), -48, 48)
					),
					new MutablePair<>(
							nbt.getDouble("delegated_spawn_point_yaw_" + i),
							nbt.getDouble("delegated_spawn_point_pitch_" + i))
			));
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

	// region --- getter & setter ---
	public List<MutablePair<BlockPos, MutablePair<Double, Double>>> getDelegatedSpawnPoints() {
		return this.delegatedSpawnPoints;
	}

	public void setDelegatedSpawnPoints(List<MutablePair<BlockPos, MutablePair<Double, Double>>> delegatedSpawnPoints) {
		this.delegatedSpawnPoints.clear();
		this.delegatedSpawnPoints.addAll(delegatedSpawnPoints);
	}
	// endregion --- getter & setter ---

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetSpawnPoint(ServerWorld serverWorld) {
		BlockPos positionOffset = new BlockPos(0, 1, 0);
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation = new MutablePair<>(0.0, 0.0);
		int index = 0;

		if (this.getWorld() != null && !this.delegatedSpawnPoints.isEmpty()) {
			index = this.getWorld().getRandom().nextInt(this.delegatedSpawnPoints.size());
		}

		if (index < this.delegatedSpawnPoints.size()) {
			positionOffset = this.delegatedSpawnPoints.get(index).getLeft();
			targetOrientation = this.delegatedSpawnPoints.get(index).getRight();
		}

		targetPos = new BlockPos(positionOffset.getX() + this.getPos().getX(), positionOffset.getY() + this.getPos().getY(), positionOffset.getZ() + this.getPos().getZ());

		if (!positionOffset.equals(BlockPos.ORIGIN) && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.rotateEntrance(delegatedSpawnPoint, blockRotation));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorEntrance(delegatedSpawnPoint, BlockMirror.FRONT_BACK));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorEntrance(delegatedSpawnPoint, BlockMirror.LEFT_RIGHT));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
