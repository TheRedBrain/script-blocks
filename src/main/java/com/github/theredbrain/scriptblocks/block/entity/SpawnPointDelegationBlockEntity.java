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
import org.apache.commons.lang3.tuple.MutablePair;

public class SpawnPointDelegationBlockEntity extends RotatedBlockEntity {
	private MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint = new MutablePair<>(new BlockPos(0, 1, 0), new MutablePair<>(0.0, 0.0));

	public SpawnPointDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.SPAWN_POINT_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("delegated_spawn_point_x", this.delegatedSpawnPoint.getLeft().getX());
		nbt.putInt("delegated_spawn_point_y", this.delegatedSpawnPoint.getLeft().getY());
		nbt.putInt("delegated_spawn_point_z", this.delegatedSpawnPoint.getLeft().getZ());
		nbt.putDouble("delegated_spawn_point_yaw", this.delegatedSpawnPoint.getRight().getLeft());
		nbt.putDouble("delegated_spawn_point_pitch", this.delegatedSpawnPoint.getRight().getRight());

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.delegatedSpawnPoint.setLeft(new BlockPos(
				nbt.getInt("delegated_spawn_point_x"),
				nbt.getInt("delegated_spawn_point_y"),
				nbt.getInt("delegated_spawn_point_z")
		));
		this.delegatedSpawnPoint.setRight(new MutablePair<>(
				nbt.getDouble("delegated_spawn_point_yaw"),
				nbt.getDouble("delegated_spawn_point_pitch")
		));

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
	public MutablePair<BlockPos, MutablePair<Double, Double>> getDelegatedSpawnPoint() {
		return this.delegatedSpawnPoint;
	}

	public boolean setDelegatedSpawnPoint(MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint) {
		this.delegatedSpawnPoint = delegatedSpawnPoint;
		return true;
	}
	// endregion --- getter & setter ---

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetSpawnPoint(ServerWorld serverWorld) {
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation;

		targetPos = new BlockPos(this.delegatedSpawnPoint.getLeft().getX() + this.getPos().getX(), this.delegatedSpawnPoint.getLeft().getY() + this.getPos().getY(), this.delegatedSpawnPoint.getLeft().getZ() + this.getPos().getZ());
		targetOrientation = this.delegatedSpawnPoint.getRight();

		if (this.delegatedSpawnPoint.getLeft() != BlockPos.ORIGIN && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.delegatedSpawnPoint = BlockRotationUtils.rotateEntrance(this.delegatedSpawnPoint, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.delegatedSpawnPoint = BlockRotationUtils.mirrorEntrance(this.delegatedSpawnPoint, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.delegatedSpawnPoint = BlockRotationUtils.mirrorEntrance(this.delegatedSpawnPoint, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
