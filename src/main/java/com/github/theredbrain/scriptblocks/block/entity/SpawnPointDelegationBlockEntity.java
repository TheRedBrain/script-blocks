package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
		super(ScriptBlocksEntities.SPAWN_POINT_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("delegated_spawn_points", NbtCompound.CODEC);
		for (MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint : this.delegatedSpawnPoints) {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", BlockPos.CODEC, spawnPoint.getLeft());
			nbtCompound.putDouble("yaw", spawnPoint.getRight().getLeft());
			nbtCompound.putDouble("pitch", spawnPoint.getRight().getRight());
			listAppender.add(nbtCompound.copy());
		}

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		this.delegatedSpawnPoints.clear();
		for (NbtCompound nbtCompound : view.getTypedListView("delegated_spawn_points", NbtCompound.CODEC)) {
			this.delegatedSpawnPoints.add(new MutablePair<>(
					nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
					new MutablePair<>(
							MathHelper.clamp(nbtCompound.getDouble("yaw", 0), -180, 180),
							MathHelper.clamp(nbtCompound.getDouble("pitch", 0), -90, 90)
					)
			));
		}

		super.readData(view);
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.rotateSpawnPoint(delegatedSpawnPoint, blockRotation));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, BlockMirror.FRONT_BACK));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<MutablePair<BlockPos, MutablePair<Double, Double>>> newDelegatedSpawnPoints = new ArrayList<>(List.of());
				for (MutablePair<BlockPos, MutablePair<Double, Double>> delegatedSpawnPoint : this.delegatedSpawnPoints) {
					newDelegatedSpawnPoints.add(BlockRotationUtils.mirrorSpawnPoint(delegatedSpawnPoint, BlockMirror.LEFT_RIGHT));
				}
				this.delegatedSpawnPoints.clear();
				this.delegatedSpawnPoints.addAll(newDelegatedSpawnPoints);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
