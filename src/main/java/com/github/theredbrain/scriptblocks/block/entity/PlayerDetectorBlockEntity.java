package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PlayerDetectorBlockEntity extends RotatedBlockEntity {

	private static final BlockPos AREA_POSITION_OFFSET_DEFAULT = new BlockPos(0, 0, 0);

	private static final BlockPos TRIGGERED_BLOCK_POS_DEFAULT = new BlockPos(0, 0, 0);
	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);

	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(TRIGGERED_BLOCK_POS_DEFAULT, false);

	private ArrayList<UUID> playerList = new ArrayList<>();

	public PlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.PLAYER_DETECTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.showArea) {
			nbt.putBoolean("showArea", true);
		} else {
			nbt.remove("showArea");
		}

		if (this.area != null) {
			nbt.putDouble("areaMinX", this.area.minX);
			nbt.putDouble("areaMaxX", this.area.maxX);
			nbt.putDouble("areaMinY", this.area.minY);
			nbt.putDouble("areaMaxY", this.area.maxY);
			nbt.putDouble("areaMinZ", this.area.minZ);
			nbt.putDouble("areaMaxZ", this.area.maxZ);
		} else {
			nbt.remove("areaMinX");
			nbt.remove("areaMaxX");
			nbt.remove("areaMinY");
			nbt.remove("areaMaxY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxZ");
		}

		if (this.areaDimensions != Vec3i.ZERO) {
			nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
			nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
			nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		}

		BlockPos areaPositionOffset = this.areaPositionOffset;
		if (!areaPositionOffset.equals(AREA_POSITION_OFFSET_DEFAULT)) {
			nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
			nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
			nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		}

		if (this.triggeredBlock.getLeft() != TRIGGERED_BLOCK_POS_DEFAULT || !this.triggeredBlock.getRight()) {
			nbt.putInt("triggeredBlockPositionOffsetX", this.triggeredBlock.getLeft().getX());
			nbt.putInt("triggeredBlockPositionOffsetY", this.triggeredBlock.getLeft().getY());
			nbt.putInt("triggeredBlockPositionOffsetZ", this.triggeredBlock.getLeft().getZ());
			nbt.putBoolean("triggeredBlockResets", this.triggeredBlock.getRight());
		} else {
			nbt.remove("triggeredBlockPositionOffsetX");
			nbt.remove("triggeredBlockPositionOffsetY");
			nbt.remove("triggeredBlockPositionOffsetZ");
			nbt.remove("triggeredBlockResets");
		}

		int playerListSize = playerList.size();
		nbt.putInt("playerListSize", playerListSize);
		for (int i = 0; i < playerListSize; i++) {
			nbt.putUuid("playerListEntry_" + i, this.playerList.get(i));
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.showArea = nbt.getBoolean("showArea");

		if (nbt.contains("areaMinX") && nbt.contains("areaMinY") && nbt.contains("areaMinZ") && nbt.contains("areaMaxX") && nbt.contains("areaMaxY") && nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
		}

		int i = MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48);
		int j = MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48);
		int k = MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48);
		this.areaDimensions = new Vec3i(i, j, k);

		int l = MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48);
		int m = MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48);
		int n = MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48);
		this.areaPositionOffset = new BlockPos(l, m, n);

		if (nbt.contains("triggeredBlockPositionOffsetX", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockPositionOffsetY", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockPositionOffsetZ", NbtElement.INT_TYPE) && nbt.contains("triggeredBlockResets", NbtElement.BYTE_TYPE)) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetX"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetY"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggeredBlockPositionOffsetZ"), -48, 48)
					),
					nbt.getBoolean("triggeredBlockResets")
			);
		}

		int playerListSize = nbt.getInt("playerListSize");
		for (i = 0; i < playerListSize; i++) {
			if (nbt.contains("playerListEntry_" + i)) {
				this.playerList.add(nbt.getUuid("playerListEntry_" + i));
			}
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

	public static void tick(World world, BlockPos pos, BlockState state, PlayerDetectorBlockEntity areaBlockEntity) {
		if (!world.isClient && world.getTime() % 20L == 0L) {
			if (areaBlockEntity.calculateAreaBox || areaBlockEntity.area == null) {
				BlockPos areaPositionOffset = areaBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = areaBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				areaBlockEntity.area = new Box(areaStart, areaEnd);
				areaBlockEntity.calculateAreaBox = false;
			}

			boolean shouldTriggerBlock = false;

			List<PlayerEntity> newPlayerList = world.getNonSpectatingEntities(PlayerEntity.class, areaBlockEntity.area);
			List<UUID> newPLayerUuidList = new ArrayList<>();
			for (PlayerEntity player : newPlayerList) {
				if (!player.isCreative() || ScriptBlocks.SERVER_CONFIG.enable_creative_player_detection) {
					newPLayerUuidList.add(player.getUuid());
				}
			}
			ArrayList<UUID> tempList = new ArrayList<>();

			Iterator<UUID> playerListIterator = areaBlockEntity.playerList.iterator();
			PlayerEntity playerEntity;
			UUID uuid;

			// old list
			while (playerListIterator.hasNext()) {
				uuid = playerListIterator.next();
				playerEntity = world.getPlayerByUuid(uuid);

				if (playerEntity != null) {
					if (newPLayerUuidList.contains(uuid)) {
						tempList.add(uuid);
						newPLayerUuidList.remove(uuid);
					}
				}
			}

			Iterator<UUID> newPlayerListIterator = newPLayerUuidList.iterator();
			// new list
			while (newPlayerListIterator.hasNext()) {
				UUID uuid2 = newPlayerListIterator.next();
				playerEntity = world.getPlayerByUuid(uuid2);

				if (playerEntity != null) {
					tempList.add(uuid2);
					shouldTriggerBlock = true;
				}
			}

			areaBlockEntity.playerList.clear();
			areaBlockEntity.playerList.addAll(tempList);

			if (shouldTriggerBlock) {
				areaBlockEntity.triggerBlock();
			}
		}
	}

	private void triggerBlock() {
		int x = this.triggeredBlock.getLeft().getX();
		int y = this.triggeredBlock.getLeft().getY();
		int z = this.triggeredBlock.getLeft().getZ();
		if (this.world != null && (x != 0 || y != 0 || z != 0)) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + x, this.pos.getY() + y, this.pos.getZ() + z));
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

	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.calculateAreaBox = true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.calculateAreaBox = true;
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
