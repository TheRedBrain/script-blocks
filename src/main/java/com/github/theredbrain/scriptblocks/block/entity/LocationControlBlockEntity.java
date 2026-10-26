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
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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
	protected void writeData(WriteView view) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.mainEntrance.getLeft());
		nbtCompound.putDouble("yaw", this.mainEntrance.getRight().getLeft());
		nbtCompound.putDouble("pitch", this.mainEntrance.getRight().getRight());
		view.put("main_entrance", NbtCompound.CODEC, nbtCompound.copy());

		WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("side_entrances", NbtCompound.CODEC);
		this.sideEntrances.forEach((key, value) -> {
			NbtCompound nbtCompound1 = new NbtCompound();
			nbtCompound1.putString("key", key);
			nbtCompound1.put("position_offset", BlockPos.CODEC, value.getLeft());
			nbtCompound1.putDouble("yaw", value.getRight().getLeft());
			nbtCompound1.putDouble("pitch", value.getRight().getRight());
			listAppender.add(nbtCompound1.copy());
		});

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", BlockPos.CODEC, this.triggeredBlock.getLeft());
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		view.put("triggered_block", NbtCompound.CODEC, nbtCompound.copy());

		view.put("data_providing_block_position_offset", BlockPos.CODEC, this.dataProvidingBlockPosOffset);

		view.putBoolean("manual_reset", this.manualReset);

		view.putBoolean("should_always_reset", this.shouldAlwaysReset);

		view.putInt("initial_reset_timer", this.initialResetTimer);

		view.putInt("reset_area_min_x", this.resetAreaMinX);

		view.putInt("reset_area_min_z", this.resetAreaMinZ);

		view.putInt("reset_area_max_x", this.resetAreaMaxX);

		view.putInt("reset_area_max_z", this.resetAreaMaxZ);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		NbtCompound nbtCompound = view.read("main_entrance", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.mainEntrance = new MutablePair<>(
				nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
				new MutablePair<>(
						MathHelper.clamp(nbtCompound.getDouble("yaw", 0), -180, 180),
						MathHelper.clamp(nbtCompound.getDouble("pitch", 0), -90, 90)
				)
		);

		this.sideEntrances.clear();
		for (NbtCompound nbtCompound1 : view.getTypedListView("side_entrances", NbtCompound.CODEC)) {
			this.sideEntrances.put(
					nbtCompound1.getString("key", ""),
					new MutablePair<>(
							nbtCompound1.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
							new MutablePair<>(
									MathHelper.clamp(nbtCompound1.getDouble("yaw", 0), -180, 180),
									MathHelper.clamp(nbtCompound1.getDouble("pitch", 0), -90, 90)
							)
					));
		}

		nbtCompound = view.read("triggered_block", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.triggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("resets", false)
		);

		this.dataProvidingBlockPosOffset = BlockPos.max(
				BlockPos.min(
						view.read("data_providing_block_pos_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.manualReset = view.getBoolean("manual_reset", false);

		this.shouldAlwaysReset = view.getBoolean("should_always_reset", false);

		this.initialResetTimer = view.getInt("initial_reset_timer", -1);

		this.resetAreaMinX = view.getInt("reset_area_min_x", RESET_AREA_MIN_X_DEFAULT);

		this.resetAreaMinZ = view.getInt("reset_area_min_z", RESET_AREA_MIN_Z_DEFAULT);

		this.resetAreaMaxX = view.getInt("reset_area_max_x", RESET_AREA_MAX_X_DEFAULT);

		this.resetAreaMaxZ = view.getInt("reset_area_max_z", RESET_AREA_MAX_Z_DEFAULT);

		super.readData(view);
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
