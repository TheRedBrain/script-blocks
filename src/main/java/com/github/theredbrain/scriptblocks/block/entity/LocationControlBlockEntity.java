package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LocationControlBlockEntity extends RotatedBlockEntity implements Resetable {
	private static final int RESET_AREA_MIN_X_DEFAULT = -16;
	private static final int RESET_AREA_MIN_Z_DEFAULT = -16;
	private static final int RESET_AREA_MAX_X_DEFAULT = 31;
	private static final int RESET_AREA_MAX_Z_DEFAULT = 31;
	private MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance = new MutablePair<>(new BlockPos(0, 1, 0), new MutablePair<>(0.0, 0.0));
	private final HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ZERO;

	public LocationControlBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.LOCATION_CONTROL_BLOCK_ENTITY, pos, state);
	}

	private boolean manualReset = false;
	private boolean shouldAlwaysReset = false;
	private int initialResetTimer = -1;

	private int resetAreaMinX = RESET_AREA_MIN_X_DEFAULT;
	private int resetAreaMinZ = RESET_AREA_MIN_Z_DEFAULT;
	private int resetAreaMaxX = RESET_AREA_MAX_X_DEFAULT;
	private int resetAreaMaxZ = RESET_AREA_MAX_Z_DEFAULT;

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.mainEntrance.getLeft());
		nbtCompound.putDouble("yaw", this.mainEntrance.getRight().getLeft());
		nbtCompound.putDouble("pitch", this.mainEntrance.getRight().getRight());
		view.store("main_entrance", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("side_entrances", CompoundTag.CODEC);
		this.sideEntrances.forEach((key, value) -> {
			CompoundTag nbtCompound1 = new CompoundTag();
			nbtCompound1.putString("key", key);
			nbtCompound1.store("position_offset", BlockPos.CODEC, value.getLeft());
			nbtCompound1.putDouble("yaw", value.getRight().getLeft());
			nbtCompound1.putDouble("pitch", value.getRight().getRight());
			listAppender.add(nbtCompound1.copy());
		});

		nbtCompound = new CompoundTag();
		nbtCompound.store("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.store("triggered_block", CompoundTag.CODEC, nbtCompound.copy());

		view.store("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putBoolean("manual_reset", this.manualReset);

		view.putBoolean("should_always_reset", this.shouldAlwaysReset);

		view.putInt("initial_reset_timer", this.initialResetTimer);

		view.putInt("reset_area_min_x", this.resetAreaMinX);

		view.putInt("reset_area_min_z", this.resetAreaMinZ);

		view.putInt("reset_area_max_x", this.resetAreaMaxX);

		view.putInt("reset_area_max_z", this.resetAreaMaxZ);

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("main_entrance", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.mainEntrance = new MutablePair<>(
				nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
				new MutablePair<>(
						Mth.clamp(nbtCompound.getDoubleOr("yaw", 0), -180, 180),
						Mth.clamp(nbtCompound.getDoubleOr("pitch", 0), -90, 90)
				)
		);

		this.sideEntrances.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("side_entrances", CompoundTag.CODEC)) {
			this.sideEntrances.put(
					nbtCompound1.getStringOr("key", ""),
					new MutablePair<>(
							nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
							new MutablePair<>(
									Mth.clamp(nbtCompound1.getDoubleOr("yaw", 0), -180, 180),
									Mth.clamp(nbtCompound1.getDoubleOr("pitch", 0), -90, 90)
							)
					));
		}

		nbtCompound = view.read("triggered_block", CompoundTag.CODEC).orElseGet(CompoundTag::new);
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

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.manualReset = view.getBooleanOr("manual_reset", false);

		this.shouldAlwaysReset = view.getBooleanOr("should_always_reset", false);

		this.initialResetTimer = view.getIntOr("initial_reset_timer", -1);

		this.resetAreaMinX = view.getIntOr("reset_area_min_x", RESET_AREA_MIN_X_DEFAULT);

		this.resetAreaMinZ = view.getIntOr("reset_area_min_z", RESET_AREA_MIN_Z_DEFAULT);

		this.resetAreaMaxX = view.getIntOr("reset_area_max_x", RESET_AREA_MAX_X_DEFAULT);

		this.resetAreaMaxZ = view.getIntOr("reset_area_max_z", RESET_AREA_MAX_Z_DEFAULT);

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public void trigger() {
		this.manualReset = false;
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

	public boolean shouldReset() {
		// TODO
		//  implement reset timer
		//  make sure resets only happen when no players are in location
		//  this could be achieved with triggered teleporter blocks
		return this.manualReset || this.shouldAlwaysReset;
	}

	@Override
	public void reset() {
		this.manualReset = true;
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	// region --- getter & setter ---
	public MutablePair<BlockPos, MutablePair<Double, Double>> getMainEntrance() {
		return mainEntrance;
	}

	public void setMainEntrance(MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance) {
		this.mainEntrance = mainEntrance;
	}

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetEntrance(ServerLevel serverWorld, String entrance) {
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation;

		if (!entrance.isEmpty() && sideEntrances.containsKey(entrance)) {
			MutablePair<BlockPos, MutablePair<Double, Double>> targetEntranceOffset = this.sideEntrances.get(entrance);
			targetPos = new BlockPos(targetEntranceOffset.getLeft().getX() + this.getBlockPos().getX(), targetEntranceOffset.getLeft().getY() + this.getBlockPos().getY(), targetEntranceOffset.getLeft().getZ() + this.getBlockPos().getZ());
			targetOrientation = targetEntranceOffset.getRight();
		} else {
			targetPos = new BlockPos(this.mainEntrance.getLeft().getX() + this.getBlockPos().getX(), this.mainEntrance.getLeft().getY() + this.getBlockPos().getY(), this.mainEntrance.getLeft().getZ() + this.getBlockPos().getZ());
			targetOrientation = this.mainEntrance.getRight();
		}

		if (serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	public HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> getSideEntrances() {
		return sideEntrances;
	}

	public void setSideEntrances(HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances) {
		this.sideEntrances.clear();
		this.sideEntrances.putAll(sideEntrances);
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	public BlockPos getDataProvidingBlockPosOffset() {
		return dataProvidingBlockPosOffset;
	}

	public void setDataProvidingBlockPosOffset(BlockPos dataProvidingBlockPosOffset) {
		this.dataProvidingBlockPosOffset = dataProvidingBlockPosOffset;
	}

	public boolean getManualReset() {
		return this.manualReset;
	}

	public void setManualReset(boolean manualReset) {
		this.manualReset = manualReset;
	}

	public boolean shouldAlwaysReset() {
		return shouldAlwaysReset;
	}

	public void setShouldAlwaysReset(boolean shouldAlwaysReset) {
		this.shouldAlwaysReset = shouldAlwaysReset;
	}

	public int getResetAreaMinX() {
		return this.resetAreaMinX;
	}

	public void setResetAreaMinX(int resetAreaMinX) {
		this.resetAreaMinX = resetAreaMinX;
	}

	public int getResetAreaMinZ() {
		return this.resetAreaMinZ;
	}

	public void setResetAreaMinZ(int resetAreaMinZ) {
		this.resetAreaMinZ = resetAreaMinZ;
	}

	public int getResetAreaMaxX() {
		return this.resetAreaMaxX;
	}

	public void setResetAreaMaxX(int resetAreaMaxX) {
		this.resetAreaMaxX = resetAreaMaxX;
	}

	public int getResetAreaMaxZ() {
		return this.resetAreaMaxZ;
	}

	public void setResetAreaMaxZ(int resetAreaMaxZ) {
		this.resetAreaMaxZ = resetAreaMaxZ;
	}

	public int getInitialResetTimer() {
		return this.initialResetTimer;
	}

	public void setInitialResetTimer(int initialResetTimer) {
		this.initialResetTimer = initialResetTimer;
	}
	// endregion --- getter & setter ---

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.mainEntrance = BlockRotationUtils.rotateSpawnPoint(this.mainEntrance, blockRotation);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> rotatedEntrance = BlockRotationUtils.rotateSpawnPoint(this.sideEntrances.get(key), blockRotation);
					this.sideEntrances.put(key, rotatedEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				Vec3i resetAreaMin = BlockRotationUtils.rotateOffsetVec3i(new Vec3i(this.resetAreaMinX, 0, this.resetAreaMinZ), blockRotation);
				this.resetAreaMinX = resetAreaMin.getX();
				this.resetAreaMinZ = resetAreaMin.getZ();

				Vec3i resetAreaMax = BlockRotationUtils.rotateOffsetVec3i(new Vec3i(this.resetAreaMaxX, 0, this.resetAreaMaxZ), blockRotation);
				this.resetAreaMaxX = resetAreaMax.getX();
				this.resetAreaMaxZ = resetAreaMax.getZ();

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorSpawnPoint(this.mainEntrance, Mirror.FRONT_BACK);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.sideEntrances.get(key), Mirror.FRONT_BACK);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.FRONT_BACK));

				Vec3i resetAreaMin = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMinX, 0, this.resetAreaMinZ), Mirror.FRONT_BACK);
				this.resetAreaMinX = resetAreaMin.getX();
				this.resetAreaMinZ = resetAreaMin.getZ();

				Vec3i resetAreaMax = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMaxX, 0, this.resetAreaMaxZ), Mirror.FRONT_BACK);
				this.resetAreaMaxX = resetAreaMax.getX();
				this.resetAreaMaxZ = resetAreaMax.getZ();

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorSpawnPoint(this.mainEntrance, Mirror.LEFT_RIGHT);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.sideEntrances.get(key), Mirror.LEFT_RIGHT);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				Vec3i resetAreaMin = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMinX, 0, this.resetAreaMinZ), Mirror.LEFT_RIGHT);
				this.resetAreaMinX = resetAreaMin.getX();
				this.resetAreaMinZ = resetAreaMin.getZ();

				Vec3i resetAreaMax = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMaxX, 0, this.resetAreaMaxZ), Mirror.LEFT_RIGHT);
				this.resetAreaMaxX = resetAreaMax.getX();
				this.resetAreaMaxZ = resetAreaMax.getZ();

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
