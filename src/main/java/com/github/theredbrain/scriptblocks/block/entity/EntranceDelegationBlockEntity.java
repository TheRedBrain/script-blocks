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
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.tuple.MutablePair;

@Deprecated
public class EntranceDelegationBlockEntity extends RotatedBlockEntity {
	private LevelData.RespawnData delegatedEntrance = LevelData.RespawnData.DEFAULT;

	public EntranceDelegationBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.ENTRANCE_DELEGATION_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.store("delegated_entrance", LevelData.RespawnData.CODEC, this.delegatedEntrance);


		ScriptBlocks.sendDeprecatedFeatureInfo("Deprecated Entrance Delegation Block detected at: " + this.worldPosition.toString() + ". This block will be removed in the future and should be replaced with a Spawn Point Delegation Block.", this.level != null ? this.level.getServer() : null);
		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.delegatedEntrance = view.read("delegated_entrance", LevelData.RespawnData.CODEC).orElseGet(
				() -> LevelData.RespawnData.of(
						this.getLevel() != null ? this.getLevel().dimension() : ServerLevel.OVERWORLD,
						new BlockPos(
								view.getIntOr("delegatedEntrance_X", 0),
								view.getIntOr("delegatedEntrance_Y", 0),
								view.getIntOr("delegatedEntrance_Z", 0)
						),
						(float) view.getDoubleOr("delegatedEntrance_Yaw", 0),
						(float) view.getDoubleOr("delegatedEntrance_Pitch", 0)
				)
		);

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
	public LevelData.RespawnData getDelegatedEntrance() {
		return this.delegatedEntrance;
	}

	public boolean setDelegatedEntrance(LevelData.RespawnData delegatedEntrance) {
		this.delegatedEntrance = LevelData.RespawnData.of(delegatedEntrance.dimension(), delegatedEntrance.pos(), delegatedEntrance.yaw(), delegatedEntrance.pitch());
		return true;
	}
	// endregion --- getter & setter ---

	public LevelData.RespawnData getTargetEntrance(ServerLevel serverWorld) {

		BlockPos targetPos = BlockRotationUtils.getActualOffsetBlockPosition(this.getBlockPos(), this.delegatedEntrance.pos());

		if (!this.delegatedEntrance.pos().equals(BlockPos.ZERO) && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return this.delegatedEntrance;
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
