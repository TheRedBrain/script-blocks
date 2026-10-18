package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationControlBlockEntity extends RotatedBlockEntity implements Resetable {
	private static final int RESET_AREA_MIN_X_DEFAULT = -16;
	private static final int RESET_AREA_MIN_Z_DEFAULT = -16;
	private static final int RESET_AREA_MAX_X_DEFAULT = 31;
	private static final int RESET_AREA_MAX_Z_DEFAULT = 31;
	private MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance = new MutablePair<>(new BlockPos(0, 1, 0), new MutablePair<>(0.0, 0.0));
	private final HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos dataProvidingBlockPosOffset = BlockPos.ORIGIN;

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
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.mainEntrance.getLeft()));
		nbtCompound.putDouble("yaw", this.mainEntrance.getRight().getLeft());
		nbtCompound.putDouble("pitch", this.mainEntrance.getRight().getRight());
		nbt.put("main_entrance", nbtCompound.copy());

		NbtList nbtList = new NbtList();
		this.sideEntrances.forEach((key, value) -> {
			NbtCompound nbtCompound1 = new NbtCompound();
			nbtCompound1.putString("key", key);
			nbtCompound1.put("position_offset", NbtHelper.fromBlockPos(value.getLeft()));
			nbtCompound1.putDouble("yaw", value.getRight().getLeft());
			nbtCompound1.putDouble("pitch", value.getRight().getRight());
			nbtList.add(nbtCompound1.copy());
		});
		nbt.put("side_entrances", nbtList);

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.triggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		nbt.put("triggered_block", nbtCompound.copy());

		nbt.put("data_providing_block_position_offset", NbtHelper.fromBlockPos(this.dataProvidingBlockPosOffset));

		nbt.putBoolean("manual_reset", this.manualReset);

		nbt.putBoolean("should_always_reset", this.shouldAlwaysReset);

		nbt.putInt("initial_reset_timer", this.initialResetTimer);

		nbt.putInt("reset_area_min_x", this.resetAreaMinX);

		nbt.putInt("reset_area_min_z", this.resetAreaMinZ);

		nbt.putInt("reset_area_max_x", this.resetAreaMaxX);

		nbt.putInt("reset_area_max_z", this.resetAreaMaxZ);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("mainEntrance_X") || nbt.contains("mainEntrance_Y") || nbt.contains("mainEntrance_Z") || nbt.contains("mainEntrance_Yaw") || nbt.contains("mainEntrance_Pitch")) {
			this.mainEntrance = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("mainEntrance_X"), -48, 48),
							MathHelper.clamp(nbt.getInt("mainEntrance_Y"), -48, 48),
							MathHelper.clamp(nbt.getInt("mainEntrance_Z"), -48, 48)
					),
					new MutablePair<>(
							nbt.getDouble("mainEntrance_Yaw"),
							nbt.getDouble("mainEntrance_Pitch"))
			);
			nbt.remove("mainEntrance_X");
			nbt.remove("mainEntrance_Y");
			nbt.remove("mainEntrance_Z");
			nbt.remove("mainEntrance_Yaw");
			nbt.remove("mainEntrance_Pitch");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("main_entrance");
			this.mainEntrance = new MutablePair<>(
					NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
					new MutablePair<>(
							MathHelper.clamp(nbtCompound.getDouble("yaw"), -180, -180),
							MathHelper.clamp(nbtCompound.getDouble("pitch"), -90, -90)
					)
			);
		}

		this.sideEntrances.clear();
		if (nbt.contains("sideEntrancesSize")) {
			int delegatedSpawnBlocksSize = nbt.getInt("sideEntrancesSize");
			for (int i = 0; i < delegatedSpawnBlocksSize; i++) {
				this.sideEntrances.put(
						nbt.getString("key_" + i),
						new MutablePair<>(
								new BlockPos(
										MathHelper.clamp(nbt.getInt("sideEntrance_" + i + "_X"), -48, 48),
										MathHelper.clamp(nbt.getInt("sideEntrance_" + i + "_Y"), -48, 48),
										MathHelper.clamp(nbt.getInt("sideEntrance_" + i + "_Z"), -48, 48)
								),
								new MutablePair<>(
										nbt.getDouble("sideEntrance_" + i + "_Yaw"),
										nbt.getDouble("sideEntrance_" + i + "_Pitch"))
						));
				nbt.remove("key_" + i);
				nbt.remove("sideEntrance_" + i + "_X");
				nbt.remove("sideEntrance_" + i + "_Y");
				nbt.remove("sideEntrance_" + i + "_Z");
				nbt.remove("sideEntrance_" + i + "_Yaw");
				nbt.remove("sideEntrance_" + i + "_Pitch");
			}
			nbt.remove("sideEntrancesSize");
		} else {
			NbtList nbtList = nbt.getList("side_entrances", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbtCompound = nbtList.getCompound(i);
				this.sideEntrances.put(
						nbtCompound.getString("key"),
						new MutablePair<>(
								NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
								new MutablePair<>(
										MathHelper.clamp(nbtCompound.getDouble("yaw"), -180, -180),
										MathHelper.clamp(nbtCompound.getDouble("pitch"), -90, -90)
								)
						));
			}
		}

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockResets")
			);
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("triggered_block");
			this.triggeredBlock = new MutablePair<>(
					BlockPos.max(
							BlockPos.min(
									NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					),
					nbtCompound.getBoolean("resets")
			);
		}

		if (nbt.contains("dataProvidingBlockPosOffsetX") || nbt.contains("dataProvidingBlockPosOffsetY") || nbt.contains("dataProvidingBlockPosOffsetZ")) {
			this.dataProvidingBlockPosOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("dataProvidingBlockPosOffsetZ"), -48, 48)
			);
			nbt.remove("dataProvidingBlockPosOffsetX");
			nbt.remove("dataProvidingBlockPosOffsetY");
			nbt.remove("dataProvidingBlockPosOffsetZ");
		} else {
			this.dataProvidingBlockPosOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "data_providing_block_pos_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		if (nbt.contains("manualReset")) {
			this.manualReset = nbt.getBoolean("manualReset");
			nbt.remove("manualReset");
		} else {
			this.manualReset = nbt.getBoolean("manual_reset");
		}

		if (nbt.contains("shouldAlwaysReset")) {
			this.shouldAlwaysReset = nbt.getBoolean("shouldAlwaysReset");
			nbt.remove("shouldAlwaysReset");
		} else {
			this.shouldAlwaysReset = nbt.getBoolean("should_always_reset");
		}

		if (nbt.contains("initialResetTimer")) {
			this.initialResetTimer = nbt.getInt("initialResetTimer");
			nbt.remove("initialResetTimer");
		} else if (nbt.contains("initial_reset_timer")) {
			this.initialResetTimer = nbt.getInt("initial_reset_timer");
			nbt.remove("initial_reset_timer");
		} else {
			this.initialResetTimer = -1;
		}

		if (nbt.contains("resetAreaMinX")) {
			this.resetAreaMinX = nbt.getInt("resetAreaMinX");
			nbt.remove("resetAreaMinX");
		} else {
			this.resetAreaMinX = nbt.getInt("reset_area_min_x");
		}

		if (nbt.contains("resetAreaMinZ")) {
			this.resetAreaMinZ = nbt.getInt("resetAreaMinZ");
			nbt.remove("resetAreaMinZ");
		} else {
			this.resetAreaMinZ = nbt.getInt("reset_area_min_z");
		}

		if (nbt.contains("resetAreaMaxX")) {
			this.resetAreaMaxX = nbt.getInt("resetAreaMaxX");
			nbt.remove("resetAreaMaxX");
		} else {
			this.resetAreaMaxX = nbt.getInt("reset_area_max_x");
		}
		if (nbt.contains("resetAreaMaxZ")) {
			this.resetAreaMaxZ = nbt.getInt("resetAreaMaxZ");
			nbt.remove("resetAreaMaxZ");
		} else {
			this.resetAreaMaxZ = nbt.getInt("reset_area_max_z");
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

	public void trigger() {
		this.manualReset = false;
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.triggeredBlock.getLeft().getX(), this.pos.getY() + this.triggeredBlock.getLeft().getY(), this.pos.getZ() + this.triggeredBlock.getLeft().getZ()));
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
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	// region --- getter & setter ---
	public MutablePair<BlockPos, MutablePair<Double, Double>> getMainEntrance() {
		return mainEntrance;
	}

	public void setMainEntrance(MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance) {
		this.mainEntrance = mainEntrance;
	}

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetEntrance(ServerWorld serverWorld, String entrance) {
		BlockPos targetPos;
		MutablePair<Double, Double> targetOrientation;

		if (!entrance.isEmpty() && sideEntrances.containsKey(entrance)) {
			MutablePair<BlockPos, MutablePair<Double, Double>> targetEntranceOffset = this.sideEntrances.get(entrance);
			targetPos = new BlockPos(targetEntranceOffset.getLeft().getX() + this.getPos().getX(), targetEntranceOffset.getLeft().getY() + this.getPos().getY(), targetEntranceOffset.getLeft().getZ() + this.getPos().getZ());
			targetOrientation = targetEntranceOffset.getRight();
		} else {
			targetPos = new BlockPos(this.mainEntrance.getLeft().getX() + this.getPos().getX(), this.mainEntrance.getLeft().getY() + this.getPos().getY(), this.mainEntrance.getLeft().getZ() + this.getPos().getZ());
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
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
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

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorSpawnPoint(this.mainEntrance, BlockMirror.FRONT_BACK);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.sideEntrances.get(key), BlockMirror.FRONT_BACK);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				Vec3i resetAreaMin = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMinX, 0, this.resetAreaMinZ), BlockMirror.FRONT_BACK);
				this.resetAreaMinX = resetAreaMin.getX();
				this.resetAreaMinZ = resetAreaMin.getZ();

				Vec3i resetAreaMax = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMaxX, 0, this.resetAreaMaxZ), BlockMirror.FRONT_BACK);
				this.resetAreaMaxX = resetAreaMax.getX();
				this.resetAreaMaxZ = resetAreaMax.getZ();

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorSpawnPoint(this.mainEntrance, BlockMirror.LEFT_RIGHT);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorSpawnPoint(this.sideEntrances.get(key), BlockMirror.LEFT_RIGHT);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				Vec3i resetAreaMin = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMinX, 0, this.resetAreaMinZ), BlockMirror.LEFT_RIGHT);
				this.resetAreaMinX = resetAreaMin.getX();
				this.resetAreaMinZ = resetAreaMin.getZ();

				Vec3i resetAreaMax = BlockRotationUtils.mirrorOffsetVec3i(new Vec3i(this.resetAreaMaxX, 0, this.resetAreaMaxZ), BlockMirror.LEFT_RIGHT);
				this.resetAreaMaxX = resetAreaMax.getX();
				this.resetAreaMaxZ = resetAreaMax.getZ();

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
