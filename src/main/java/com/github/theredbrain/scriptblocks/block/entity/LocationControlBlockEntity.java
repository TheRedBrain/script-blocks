package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationControlBlockEntity extends RotatedBlockEntity implements Resetable {
	private MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance = new MutablePair<>(new BlockPos(0, 1, 0), new MutablePair<>(0.0, 0.0));
	private HashMap<String, MutablePair<BlockPos, MutablePair<Double, Double>>> sideEntrances = new HashMap<>(Map.of());
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(new BlockPos(0, 0, 0), false);

	public LocationControlBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.LOCATION_CONTROL_BLOCK_ENTITY, pos, state);
	}

	private boolean manualReset = false;
	private boolean shouldAlwaysReset = false;
	private int initialResetTimer = -1;

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("mainEntrance_X", this.mainEntrance.getLeft().getX());
		nbt.putInt("mainEntrance_Y", this.mainEntrance.getLeft().getY());
		nbt.putInt("mainEntrance_Z", this.mainEntrance.getLeft().getZ());
		nbt.putDouble("mainEntrance_Yaw", this.mainEntrance.getRight().getLeft());
		nbt.putDouble("mainEntrance_Pitch", this.mainEntrance.getRight().getRight());

		List<String> keyList = this.sideEntrances.keySet().stream().toList();
		int sideEntrancesSize = this.sideEntrances.keySet().size();
		nbt.putInt("sideEntrancesSize", sideEntrancesSize);
		for (int i = 0; i < sideEntrancesSize; i++) {
			String key = keyList.get(i);
			nbt.putString("key_" + i, key);
			nbt.putInt("sideEntrance_" + i + "_X", this.sideEntrances.get(key).getLeft().getX());
			nbt.putInt("sideEntrance_" + i + "_Y", this.sideEntrances.get(key).getLeft().getY());
			nbt.putInt("sideEntrance_" + i + "_Z", this.sideEntrances.get(key).getLeft().getZ());
			nbt.putDouble("sideEntrance_" + i + "_Yaw", this.sideEntrances.get(key).getRight().getLeft());
			nbt.putDouble("sideEntrance_" + i + "_Pitch", this.sideEntrances.get(key).getRight().getRight());
		}

		nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
		nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
		nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
		nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());

		nbt.putBoolean("manualReset", this.manualReset);

		nbt.putBoolean("shouldAlwaysReset", this.shouldAlwaysReset);

		nbt.putInt("initialResetTimer", this.initialResetTimer);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int mainEntrance_X = nbt.getInt("mainEntrance_X");
		int mainEntrance_Y = nbt.getInt("mainEntrance_Y");
		int mainEntrance_Z = nbt.getInt("mainEntrance_Z");
		this.mainEntrance.setLeft(new BlockPos(mainEntrance_X, mainEntrance_Y, mainEntrance_Z));
		double mainEntrance_Yaw = nbt.getDouble("mainEntrance_Yaw");
		double mainEntrance_Pitch = nbt.getDouble("mainEntrance_Pitch");
		this.mainEntrance.setRight(new MutablePair<>(mainEntrance_Yaw, mainEntrance_Pitch));

		int sideEntrancesSize = nbt.getInt("sideEntrancesSize");
		this.sideEntrances = new HashMap<>(Map.of());
		for (int i = 0; i < sideEntrancesSize; i++) {
			String key = nbt.getString("key_" + i);
			int sideEntranceX = nbt.getInt("sideEntrance_" + i + "_X");
			int sideEntranceY = nbt.getInt("sideEntrance_" + i + "_Y");
			int sideEntranceZ = nbt.getInt("sideEntrance_" + i + "_Z");
			double sideEntranceYaw = nbt.getDouble("sideEntrance_" + i + "_Yaw");
			double sideEntrancePitch = nbt.getDouble("sideEntrance_" + i + "_Pitch");
			this.sideEntrances.put(key, new MutablePair<>(new BlockPos(sideEntranceX, sideEntranceY, sideEntranceZ), new MutablePair<>(sideEntranceYaw, sideEntrancePitch)));
		}

		int x = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48);
		int y = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48);
		int z = MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48);
		this.triggeredBlock = new MutablePair<>(new BlockPos(x, y, z), nbt.getBoolean("triggeredBlockResets"));

		this.manualReset = nbt.getBoolean("manualReset");

		this.shouldAlwaysReset = nbt.getBoolean("shouldAlwaysReset");

		this.initialResetTimer = nbt.contains("initialResetTimer", NbtElement.INT_TYPE) ? nbt.getInt("initialResetTimer") : -1;

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public MutablePair<BlockPos, MutablePair<Double, Double>> getMainEntrance() {
		return mainEntrance;
	}

	public void setMainEntrance(MutablePair<BlockPos, MutablePair<Double, Double>> mainEntrance) {
		this.mainEntrance = mainEntrance;
	}

	public MutablePair<BlockPos, MutablePair<Double, Double>> getTargetEntrance(String entrance) {
		MutablePair<BlockPos, MutablePair<Double, Double>> targetEntrance;
		if (!entrance.equals("") && sideEntrances.containsKey(entrance)) {
			MutablePair<BlockPos, MutablePair<Double, Double>> targetEntranceOffset = this.sideEntrances.get(entrance);
			targetEntrance = new MutablePair<>(new BlockPos(targetEntranceOffset.getLeft().getX() + this.getPos().getX(), targetEntranceOffset.getLeft().getY() + this.getPos().getY(), targetEntranceOffset.getLeft().getZ() + this.getPos().getZ()), targetEntranceOffset.getRight());
		} else {
			targetEntrance = new MutablePair<>(new BlockPos(this.mainEntrance.getLeft().getX() + this.getPos().getX(), this.mainEntrance.getLeft().getY() + this.getPos().getY(), this.mainEntrance.getLeft().getZ() + this.getPos().getZ()), this.mainEntrance.getRight());
		}
		return targetEntrance;
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
		return this.manualReset || this.shouldAlwaysReset;
	}

	@Override
	public void reset() {
		this.manualReset = true;
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

	public int getInitialResetTimer() {
		return this.initialResetTimer;
	}

	public void setInitialResetTimer(int initialResetTimer) {
		this.initialResetTimer = initialResetTimer;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.mainEntrance = BlockRotationUtils.rotateEntrance(this.mainEntrance, blockRotation);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> rotatedEntrance = BlockRotationUtils.rotateEntrance(this.sideEntrances.get(key), blockRotation);
					this.sideEntrances.put(key, rotatedEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorEntrance(this.mainEntrance, BlockMirror.FRONT_BACK);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorEntrance(this.sideEntrances.get(key), BlockMirror.FRONT_BACK);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.mainEntrance = BlockRotationUtils.mirrorEntrance(this.mainEntrance, BlockMirror.LEFT_RIGHT);

				List<String> keyList = this.sideEntrances.keySet().stream().toList();
				int sideEntrancesSize = this.sideEntrances.keySet().size();
				for (int i = 0; i < sideEntrancesSize; i++) {
					String key = keyList.get(i);
					MutablePair<BlockPos, MutablePair<Double, Double>> mirroredEntrance = BlockRotationUtils.mirrorEntrance(this.sideEntrances.get(key), BlockMirror.LEFT_RIGHT);
					this.sideEntrances.put(key, mirroredEntrance);
				}

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
