package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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

public class PlayerDetectorBlockEntity extends RotatedBlockEntity implements Triggerable {

	private boolean enableTicking = true;

	private boolean calculateAreaBox = true;
	private Box area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = BlockPos.ORIGIN;

	private MutablePair<BlockPos, Boolean> onEnteringTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> onLeavingTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> onTriggeringTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos onEnteringUUIDListHandler = BlockPos.ORIGIN;
	private BlockPos onLeavingUUIDListHandler = BlockPos.ORIGIN;
	private BlockPos onTriggeringUUIDListHandler = BlockPos.ORIGIN;

	private final ArrayList<UUID> playerList = new ArrayList<>();

	public PlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.PLAYER_DETECTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putBoolean("enable_ticking", this.enableTicking);

		nbt.putBoolean("show_area", this.showArea);

		if (this.area != null) {
			nbt.putDouble("area_min_x", this.area.minX);
			nbt.putDouble("area_max_x", this.area.maxX);
			nbt.putDouble("area_min_y", this.area.minY);
			nbt.putDouble("area_max_y", this.area.maxY);
			nbt.putDouble("area_min_z", this.area.minZ);
			nbt.putDouble("area_max_z", this.area.maxZ);
		}

		nbt.putInt("area_dimensions_x", this.areaDimensions.getX());
		nbt.putInt("area_dimensions_y", this.areaDimensions.getY());
		nbt.putInt("area_dimensions_z", this.areaDimensions.getZ());

		nbt.putInt("area_position_offset_x", this.areaPositionOffset.getX());
		nbt.putInt("area_position_offset_y", this.areaPositionOffset.getY());
		nbt.putInt("area_position_offset_z", this.areaPositionOffset.getZ());

		nbt.putInt("on_entering_triggered_block_position_offset_x", this.onEnteringTriggeredBlock.getLeft().getX());
		nbt.putInt("on_entering_triggered_block_position_offset_y", this.onEnteringTriggeredBlock.getLeft().getY());
		nbt.putInt("on_entering_triggered_block_position_offset_z", this.onEnteringTriggeredBlock.getLeft().getZ());
		nbt.putBoolean("on_entering_triggered_block_resets", this.onEnteringTriggeredBlock.getRight());

		nbt.putInt("on_leaving_triggered_block_position_offset_x", this.onLeavingTriggeredBlock.getLeft().getX());
		nbt.putInt("on_leaving_triggered_block_position_offset_y", this.onLeavingTriggeredBlock.getLeft().getY());
		nbt.putInt("on_leaving_triggered_block_position_offset_z", this.onLeavingTriggeredBlock.getLeft().getZ());
		nbt.putBoolean("on_leaving_triggered_block_resets", this.onLeavingTriggeredBlock.getRight());

		nbt.putInt("on_triggering_triggered_block_position_offset_x", this.onTriggeringTriggeredBlock.getLeft().getX());
		nbt.putInt("on_triggering_triggered_block_position_offset_y", this.onTriggeringTriggeredBlock.getLeft().getY());
		nbt.putInt("on_triggering_triggered_block_position_offset_z", this.onTriggeringTriggeredBlock.getLeft().getZ());
		nbt.putBoolean("on_triggering_triggered_block_resets", this.onTriggeringTriggeredBlock.getRight());

		nbt.putInt("on_entering_uuid_list_handler_position_offset_x", this.onEnteringUUIDListHandler.getX());
		nbt.putInt("on_entering_uuid_list_handler_position_offset_y", this.onEnteringUUIDListHandler.getY());
		nbt.putInt("on_entering_uuid_list_handler_position_offset_z", this.onEnteringUUIDListHandler.getZ());

		nbt.putInt("on_leaving_uuid_list_handler_position_offset_x", this.onLeavingUUIDListHandler.getX());
		nbt.putInt("on_leaving_uuid_list_handler_position_offset_y", this.onLeavingUUIDListHandler.getY());
		nbt.putInt("on_leaving_uuid_list_handler_position_offset_z", this.onLeavingUUIDListHandler.getZ());

		nbt.putInt("on_triggering_uuid_list_handler_position_offset_x", this.onTriggeringUUIDListHandler.getX());
		nbt.putInt("on_triggering_uuid_list_handler_position_offset_y", this.onTriggeringUUIDListHandler.getY());
		nbt.putInt("on_triggering_uuid_list_handler_position_offset_z", this.onTriggeringUUIDListHandler.getZ());

		int playerListSize = playerList.size();
		nbt.putInt("player_list_size", playerListSize);
		for (int i = 0; i < playerListSize; i++) {
			nbt.putUuid("player_list_entry_" + i, this.playerList.get(i));
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.enableTicking = nbt.getBoolean("enable_ticking");

		if (nbt.contains("showArea")) {
			this.showArea = nbt.getBoolean("showArea");
			nbt.remove("showArea");
		} else {
			this.showArea = nbt.getBoolean("show_area");
		}

		if (nbt.contains("areaMinX") || nbt.contains("areaMinY") || nbt.contains("areaMinZ") || nbt.contains("areaMaxX") || nbt.contains("areaMaxY") || nbt.contains("areaMaxZ")) {
			this.area = new Box(nbt.getDouble("areaMinX"), nbt.getDouble("areaMinY"), nbt.getDouble("areaMinZ"), nbt.getDouble("areaMaxX"), nbt.getDouble("areaMaxY"), nbt.getDouble("areaMaxZ"));
			this.calculateAreaBox = true;
			nbt.remove("areaMinX");
			nbt.remove("areaMinY");
			nbt.remove("areaMinZ");
			nbt.remove("areaMaxX");
			nbt.remove("areaMaxY");
			nbt.remove("areaMaxZ");
		} else {
			this.area = new Box(nbt.getDouble("area_min_x"), nbt.getDouble("area_min_y"), nbt.getDouble("area_min_z"), nbt.getDouble("area_max_x"), nbt.getDouble("area_max_y"), nbt.getDouble("area_max_z"));
			this.calculateAreaBox = true;
		}

		if (nbt.contains("areaDimensionsX") || nbt.contains("areaDimensionsY") || nbt.contains("areaDimensionsZ")) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48)
			);
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		} else {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("area_dimensions_x"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_y"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_z"), 0, 48)
			);
		}

		if (nbt.contains("areaPositionOffsetX") || nbt.contains("areaPositionOffsetY") || nbt.contains("areaPositionOffsetZ")) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48)
			);
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		} else {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("area_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_z"), -48, 48)
			);
		}

		if (nbt.contains("triggeredBlockPositionOffsetX") || nbt.contains("triggeredBlockPositionOffsetY") || nbt.contains("triggeredBlockPositionOffsetZ") || nbt.contains("triggeredBlockResets")) {
			this.onEnteringTriggeredBlock = new MutablePair<>(
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
			this.onEnteringTriggeredBlock = new MutablePair<>(
					new BlockPos(
							MathHelper.clamp(nbt.getInt("on_entering_triggered_block_position_offset_x"), -48, 48),
							MathHelper.clamp(nbt.getInt("on_entering_triggered_block_position_offset_y"), -48, 48),
							MathHelper.clamp(nbt.getInt("on_entering_triggered_block_position_offset_z"), -48, 48)
					),
					nbt.getBoolean("on_entering_triggered_block_resets")
			);
		}

		this.onLeavingTriggeredBlock = new MutablePair<>(
				new BlockPos(
						MathHelper.clamp(nbt.getInt("on_leaving_triggered_block_position_offset_x"), -48, 48),
						MathHelper.clamp(nbt.getInt("on_leaving_triggered_block_position_offset_y"), -48, 48),
						MathHelper.clamp(nbt.getInt("on_leaving_triggered_block_position_offset_z"), -48, 48)
				),
				nbt.getBoolean("on_leaving_triggered_block_resets")
		);

		this.onTriggeringTriggeredBlock = new MutablePair<>(
				new BlockPos(
						MathHelper.clamp(nbt.getInt("on_triggering_triggered_block_position_offset_x"), -48, 48),
						MathHelper.clamp(nbt.getInt("on_triggering_triggered_block_position_offset_y"), -48, 48),
						MathHelper.clamp(nbt.getInt("on_triggering_triggered_block_position_offset_z"), -48, 48)
				),
				nbt.getBoolean("on_triggering_triggered_block_resets")
		);

		this.onEnteringUUIDListHandler = new BlockPos(
				MathHelper.clamp(nbt.getInt("on_entering_uuid_list_handler_position_offset_x"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_entering_uuid_list_handler_position_offset_y"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_entering_uuid_list_handler_position_offset_z"), -48, 48)
		);

		this.onLeavingUUIDListHandler = new BlockPos(
				MathHelper.clamp(nbt.getInt("on_leaving_uuid_list_handler_position_offset_x"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_leaving_uuid_list_handler_position_offset_y"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_leaving_uuid_list_handler_position_offset_z"), -48, 48)
		);

		this.onTriggeringUUIDListHandler = new BlockPos(
				MathHelper.clamp(nbt.getInt("on_triggering_uuid_list_handler_position_offset_x"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_triggering_uuid_list_handler_position_offset_y"), -48, 48),
				MathHelper.clamp(nbt.getInt("on_triggering_uuid_list_handler_position_offset_z"), -48, 48)
		);

		if (nbt.contains("playerListSize")) {
			int playerListSize = nbt.getInt("playerListSize");
			for (int i = 0; i < playerListSize; i++) {
				if (nbt.contains("playerListEntry_" + i)) {
					this.playerList.add(nbt.getUuid("playerListEntry_" + i));
					nbt.remove("playerListEntry_" + i);
				}
			}
			nbt.remove("playerListSize");
		} else {
			int playerListSize = nbt.getInt("player_list_size");
			for (int i = 0; i < playerListSize; i++) {
				if (nbt.contains("player_list_entry_" + i)) {
					this.playerList.add(nbt.getUuid("player_list_entry_" + i));
				}
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

	public static void tick(World world, BlockPos pos, BlockState state, PlayerDetectorBlockEntity playerDetectorBlockEntity) {
		if (!world.isClient() && playerDetectorBlockEntity.enableTicking() && world.getTime() % 20L == 0L) {
			if (playerDetectorBlockEntity.calculateAreaBox || playerDetectorBlockEntity.area == null) {
				BlockPos areaPositionOffset = playerDetectorBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = playerDetectorBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				playerDetectorBlockEntity.area = new Box(areaStart, areaEnd);
				playerDetectorBlockEntity.calculateAreaBox = false;
			}

			List<PlayerEntity> newCurrentPlayerList = world.getNonSpectatingEntities(PlayerEntity.class, playerDetectorBlockEntity.area);
			List<UUID> enteringPlayersUuidList = new ArrayList<>();
			List<UUID> leavingPlayersUuidList = new ArrayList<>();
			for (PlayerEntity player : newCurrentPlayerList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					enteringPlayersUuidList.add(player.getUuid());
				}
			}
			ArrayList<UUID> newCurrentPlayerUUIDList = new ArrayList<>();

			Iterator<UUID> oldCurrentPlayerListIterator = playerDetectorBlockEntity.playerList.iterator();
			PlayerEntity playerEntity;
			UUID uuid;

			// old list
			while (oldCurrentPlayerListIterator.hasNext()) {
				uuid = oldCurrentPlayerListIterator.next();
				playerEntity = world.getPlayerByUuid(uuid);

				if (playerEntity != null) {
					if (enteringPlayersUuidList.contains(uuid)) {
						newCurrentPlayerUUIDList.add(uuid);
						enteringPlayersUuidList.remove(uuid);
					} else {
						leavingPlayersUuidList.add(uuid);
					}
				}
			}

			if (!enteringPlayersUuidList.isEmpty()) {
				playerDetectorBlockEntity.sendPlayerUUIDListOnEntering(enteringPlayersUuidList);
				playerDetectorBlockEntity.triggerBlockOnEntering();
			}
			if (!leavingPlayersUuidList.isEmpty()) {
				playerDetectorBlockEntity.sendPlayerUUIDListOnLeaving(leavingPlayersUuidList);
				playerDetectorBlockEntity.triggerBlockOnLeaving();
			}

			playerDetectorBlockEntity.playerList.clear();
			playerDetectorBlockEntity.playerList.addAll(newCurrentPlayerUUIDList);

			playerDetectorBlockEntity.markDirty();
		}
	}

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	private void triggerBlockOnEntering() {
		if (this.world != null && !this.onEnteringTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringTriggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.onEnteringTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	private void triggerBlockOnLeaving() {
		if (this.world != null && !this.onLeavingTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onLeavingTriggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.onLeavingTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	private void triggerBlockOnTriggering() {
		if (this.world != null && !this.onTriggeringTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onTriggeringTriggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.onTriggeringTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	public void sendPlayerUUIDListOnEntering(List<UUID> uuidList) {
		if (this.world != null && !this.onEnteringUUIDListHandler.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnLeaving(List<UUID> uuidList) {
		if (this.world != null && !this.onLeavingUUIDListHandler.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onLeavingUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnTriggering(List<UUID> uuidList) {
		if (this.world != null && !this.onTriggeringUUIDListHandler.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onTriggeringUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	// region --- getter & setter ---
	public boolean enableTicking() {
		return this.enableTicking;
	}

	public void setEnableTicking(boolean enableTicking) {
		this.enableTicking = enableTicking;
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

	public MutablePair<BlockPos, Boolean> getOnEnteringTriggeredBlock() {
		return this.onEnteringTriggeredBlock;
	}

	public void setOnEnteringTriggeredBlock(MutablePair<BlockPos, Boolean> onEnteringTriggeredBlock) {
		this.onEnteringTriggeredBlock = onEnteringTriggeredBlock;
	}

	public MutablePair<BlockPos, Boolean> getOnLeavingTriggeredBlock() {
		return this.onLeavingTriggeredBlock;
	}

	public void setOnLeavingTriggeredBlock(MutablePair<BlockPos, Boolean> onLeavingTriggeredBlock) {
		this.onLeavingTriggeredBlock = onLeavingTriggeredBlock;
	}

	public MutablePair<BlockPos, Boolean> getOnTriggeringTriggeredBlock() {
		return this.onTriggeringTriggeredBlock;
	}

	public void setOnTriggeringTriggeredBlock(MutablePair<BlockPos, Boolean> onTriggeringTriggeredBlock) {
		this.onTriggeringTriggeredBlock = onTriggeringTriggeredBlock;
	}

	public BlockPos getOnEnteringUUIDListHandler() {
		return this.onEnteringUUIDListHandler;
	}

	public void setOnEnteringUUIDListHandler(BlockPos onEnteringUUIDListHandler) {
		this.onEnteringUUIDListHandler = onEnteringUUIDListHandler;
	}

	public BlockPos getOnLeavingUUIDListHandler() {
		return this.onLeavingUUIDListHandler;
	}

	public void setOnLeavingUUIDListHandler(BlockPos onLeavingUUIDListHandler) {
		this.onLeavingUUIDListHandler = onLeavingUUIDListHandler;
	}

	public BlockPos getOnTriggeringUUIDListHandler() {
		return this.onTriggeringUUIDListHandler;
	}

	public void setOnTriggeringUUIDListHandler(BlockPos onTriggeringUUIDListHandler) {
		this.onTriggeringUUIDListHandler = onTriggeringUUIDListHandler;
	}

	// endregion --- getter & setter ---

	@Override
	public void trigger() {
		if (this.getWorld() != null && !this.getWorld().isClient()) {
			if (this.calculateAreaBox || this.area == null) {
				BlockPos areaPositionOffset = this.areaPositionOffset;
				Vec3i areaDimensions = this.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				this.area = new Box(areaStart, areaEnd);
				this.calculateAreaBox = false;
			}

			List<PlayerEntity> currentPlayerList = this.getWorld().getNonSpectatingEntities(PlayerEntity.class, this.area);
			List<UUID> currentPlayersUuidList = new ArrayList<>();
			for (PlayerEntity player : currentPlayerList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					currentPlayersUuidList.add(player.getUuid());
				}
			}

			if (!currentPlayersUuidList.isEmpty()) {
				this.sendPlayerUUIDListOnTriggering(currentPlayersUuidList);
				this.triggerBlockOnTriggering();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), blockRotation));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), blockRotation));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), blockRotation));

				this.onEnteringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringUUIDListHandler, blockRotation);
				this.onLeavingUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingUUIDListHandler, blockRotation);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringUUIDListHandler, blockRotation);

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, BlockMirror.FRONT_BACK);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, BlockMirror.FRONT_BACK);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, BlockMirror.LEFT_RIGHT);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, BlockMirror.LEFT_RIGHT);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
