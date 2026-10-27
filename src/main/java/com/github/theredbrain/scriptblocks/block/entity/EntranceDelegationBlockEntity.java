package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

@Deprecated
public class EntranceDelegationBlockEntity extends RotatedBlockEntity {
	private MutablePair<BlockPos, MutablePair<Double, Double>> delegatedEntrance = new MutablePair<>(new BlockPos(0, 1, 0), new MutablePair<>(0.0, 0.0));

	public EntranceDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.ENTRANCE_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putInt("delegatedEntrance_X", this.delegatedEntrance.getLeft().getX());
		view.putInt("delegatedEntrance_Y", this.delegatedEntrance.getLeft().getY());
		view.putInt("delegatedEntrance_Z", this.delegatedEntrance.getLeft().getZ());
		view.putDouble("delegatedEntrance_Yaw", this.delegatedEntrance.getRight().getLeft());
		view.putDouble("delegatedEntrance_Pitch", this.delegatedEntrance.getRight().getRight());


		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Entrance Delegation Block detected at: " + this.worldPosition.toString() + ". This block will be removed in the future and should be replaced with a Spawn Point Delegation Block.", this.level != null ? this.level.getServer() : null);
		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		int delegatedEntrance_X = view.getIntOr("delegatedEntrance_X", 0);
		int delegatedEntrance_Y = view.getIntOr("delegatedEntrance_Y", 0);
		int delegatedEntrance_Z = view.getIntOr("delegatedEntrance_Z", 0);
		this.delegatedEntrance.setLeft(new BlockPos(delegatedEntrance_X, delegatedEntrance_Y, delegatedEntrance_Z));
		double delegatedEntrance_Yaw = view.getDoubleOr("delegatedEntrance_Yaw", 0);
		double delegatedEntrance_Pitch = view.getDoubleOr("delegatedEntrance_Pitch", 0);
		this.delegatedEntrance.setRight(new MutablePair<>(delegatedEntrance_Yaw, delegatedEntrance_Pitch));

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
	public MutablePair<BlockPos, MutablePair<Double, Double>> getDelegatedEntrance() {
		return this.delegatedEntrance;
	}

	public boolean setDelegatedEntrance(MutablePair<BlockPos, MutablePair<Double, Double>> delegatedEntrance) {
		this.delegatedEntrance = delegatedEntrance;
		return true;
	}
	// endregion --- getter & setter ---

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetEntrance(ServerLevel serverWorld) {
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation;

		targetPos = new BlockPos(this.delegatedEntrance.getLeft().getX() + this.getBlockPos().getX(), this.delegatedEntrance.getLeft().getY() + this.getBlockPos().getY(), this.delegatedEntrance.getLeft().getZ() + this.getBlockPos().getZ());
		targetOrientation = this.delegatedEntrance.getRight();

		if (this.delegatedEntrance.getLeft() != BlockPos.ZERO && serverWorld.getBlockEntity(targetPos) instanceof EntranceDelegationBlockEntity entranceDelegationBlockEntity) {
			return entranceDelegationBlockEntity.getTargetEntrance(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.delegatedEntrance = BlockRotationUtils.rotateSpawnPoint(this.delegatedEntrance, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.delegatedEntrance = BlockRotationUtils.mirrorSpawnPoint(this.delegatedEntrance, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.delegatedEntrance = BlockRotationUtils.mirrorSpawnPoint(this.delegatedEntrance, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
