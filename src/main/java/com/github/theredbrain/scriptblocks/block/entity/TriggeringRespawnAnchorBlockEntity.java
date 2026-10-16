package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TriggeringRespawnAnchorBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint = new MutablePair<>(BlockPos.ORIGIN, new MutablePair<>(0.0, 0.0));
	private MutablePair<BlockPos, Boolean> triggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private final Set<UUID> uuidList = new HashSet<>();

	public TriggeringRespawnAnchorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public TriggeringRespawnAnchorBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.TRIGGERING_RESPAWN_ANCHOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.spawnPoint.getLeft()));
		nbtCompound.putDouble("yaw", this.spawnPoint.getRight().getLeft());
		nbtCompound.putDouble("pitch", this.spawnPoint.getRight().getRight());
		nbt.put("spawn_point", nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.triggeredBlock.getLeft()));
		nbtCompound.putBoolean("resets", this.triggeredBlock.getRight());
		nbt.put("triggered_block", nbtCompound);

		List<UUID> list = this.uuidList.stream().toList();
		nbt.putInt("uuid_list_size", list.size());
		for (int i = 0; i < list.size(); i++) {
			nbt.putUuid("uuid_" + i, list.get(i));
		}

		NbtList nbtList = new NbtList();
		for (UUID uuid : this.uuidList) {
			nbtList.add(NbtHelper.fromUuid(uuid));
		}
		nbt.put("uuid_list", nbtList);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("spawn_point_x") || nbt.contains("spawn_point_y") || nbt.contains("spawn_point_z") || nbt.contains("spawn_point_yaw") || nbt.contains("spawn_point_pitch")) {
			this.spawnPoint = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("spawn_point_x"), -48, 48),
							MathHelper.clamp(nbt.getInt("spawn_point_y"), -48, 48),
							MathHelper.clamp(nbt.getInt("spawn_point_z"), -48, 48)
					),
					new MutablePair<>(
							nbt.getDouble("spawn_point_yaw"),
							nbt.getDouble("spawn_point_pitch"))
			);
			nbt.remove("spawn_point_x");
			nbt.remove("spawn_point_y");
			nbt.remove("spawn_point_z");
			nbt.remove("spawn_point_yaw");
			nbt.remove("spawn_point_pitch");
		} else {
			NbtCompound nbtCompound = nbt.getCompound("spawn_point");
			this.spawnPoint = new MutablePair<>(
					NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
					new MutablePair<>(
							MathHelper.clamp(nbtCompound.getDouble("yaw"), -180, 180),
							MathHelper.clamp(nbtCompound.getDouble("pitch"), -90, 90)
					)
			);
		}

		if (nbt.contains("triggered_block_position_offset_x") || nbt.contains("triggered_block_position_offset_y") || nbt.contains("triggered_block_position_offset_z")) {
			this.triggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_x"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_y"), -48, 48),
							MathHelper.clamp(nbt.getInt("triggered_block_position_offset_z"), -48, 48)
					),
					nbt.getBoolean("triggered_block_resets")
			);
			nbt.remove("triggered_block_position_offset_x");
			nbt.remove("triggered_block_position_offset_y");
			nbt.remove("triggered_block_position_offset_z");
			nbt.remove("triggered_block_resets");
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

		this.uuidList.clear();
		if (nbt.contains("uuid_list_size")) {
			int listSize = nbt.getInt("uuid_list_size");
			for (int i = 0; i < listSize; i++) {
				if (nbt.containsUuid("uuid_" + i)) {
					this.uuidList.add(nbt.getUuid("uuid_" + i));
				}
				nbt.remove("uuid_" + i);
			}
			nbt.getBoolean("uuid_list_size");
		} else {
			NbtList nbtList = nbt.getList("uuid_list", NbtElement.INT_ARRAY_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				this.uuidList.add(NbtHelper.toUuid(nbtList.get(i)));
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

	// region --- getter & setter ---
	public MutablePair<BlockPos, MutablePair<Double, Double>> getSpawnPoint() {
		return this.spawnPoint;
	}

	public void setSpawnPoint(MutablePair<BlockPos, MutablePair<Double, Double>> spawnPoint) {
		this.spawnPoint = spawnPoint;
	}

	public MutablePair<BlockPos, Boolean> getTriggeredBlock() {
		return this.triggeredBlock;
	}

	public void setTriggeredBlock(MutablePair<BlockPos, Boolean> triggeredBlock) {
		this.triggeredBlock = triggeredBlock;
	}
	// endregion --- getter & setter ---

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	public MutablePair<BlockPos, MutablePair<Double, Double>> getActualSpawnPoint(ServerWorld serverWorld) {
		MutablePair<Double, Double> targetOrientation = this.spawnPoint.getRight();

		BlockPos targetPos = getActualOffsetBlockPosition(this.getPos(), this.spawnPoint.getLeft());

		if (!this.spawnPoint.getLeft().equals(BlockPos.ORIGIN) && serverWorld.getBlockEntity(targetPos) instanceof SpawnPointDelegationBlockEntity spawnPointDelegationBlockEntity) {
			return spawnPointDelegationBlockEntity.getTargetSpawnPoint(serverWorld);
		}

		return new MutablePair<>(targetPos, targetOrientation);
	}

	public void trigger() {
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

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		return List.of();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		Iterator<UUID> listIterator = list.iterator();
		PlayerEntity playerEntity;
		UUID uuid;
		if (this.getWorld() != null) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = this.getWorld().getPlayerByUuid(uuid);

				if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
					serverPlayerEntity.setSpawnPoint(this.getWorld().getRegistryKey(), remove ? null : this.pos, 0.0F, false, !remove);
				}
			}
		}

		if (remove) {
			list.forEach(this.uuidList::remove);
		} else {
			this.uuidList.addAll(list);
		}

		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	public void reset() {

		Iterator<UUID> listIterator = this.uuidList.iterator();
		PlayerEntity playerEntity;
		UUID uuid;
		if (this.getWorld() != null) {
			while (listIterator.hasNext()) {
				uuid = listIterator.next();
				playerEntity = this.getWorld().getPlayerByUuid(uuid);

				if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
					serverPlayerEntity.setSpawnPoint(this.getWorld().getRegistryKey(), null, 0.0F, false, false);
				}
			}
		}

		this.uuidList.clear();
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.spawnPoint = BlockRotationUtils.rotateSpawnPoint(this.spawnPoint, blockRotation);
				this.triggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.triggeredBlock.getLeft(), blockRotation));
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, BlockMirror.FRONT_BACK);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.spawnPoint = BlockRotationUtils.mirrorSpawnPoint(this.spawnPoint, BlockMirror.LEFT_RIGHT);
				this.triggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.triggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
