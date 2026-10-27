package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class PlayerDetectorBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

	private boolean enableTicking = true;

	private boolean calculateAreaBox = true;
	private AABB area = null;
	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = BlockPos.ZERO;

	private MutablePair<BlockPos, Boolean> onEnteringTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> onLeavingTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> onTriggeringTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private BlockPos onEnteringUUIDListHandler = BlockPos.ZERO;
	private BlockPos onLeavingUUIDListHandler = BlockPos.ZERO;
	private BlockPos onTriggeringUUIDListHandler = BlockPos.ZERO;

	private final ArrayList<UUID> playerList = new ArrayList<>();

	public PlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.PLAYER_DETECTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		view.putBoolean("enable_ticking", this.enableTicking);

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringTriggeredBlock.getRight());
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringUUIDListHandler);
		view.store("on_entering", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.onLeavingTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onLeavingTriggeredBlock.getRight());
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onLeavingUUIDListHandler);
		view.store("on_leaving", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.onTriggeringTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onTriggeringTriggeredBlock.getRight());
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onTriggeringUUIDListHandler);
		view.store("on_triggering", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<UUID> listAppender = view.list("player_list", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.playerList) {
			listAppender.add(uuid);
		}

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.enableTicking = view.getBooleanOr("enable_ticking", false);

		CompoundTag nbtCompound = view.read("area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showArea = nbtCompound.getBooleanOr("visible", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onEnteringTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		this.onEnteringUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);


		nbtCompound = view.read("on_leaving", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onLeavingTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		this.onLeavingUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_triggering", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onTriggeringTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		this.onTriggeringUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.playerList.clear();
		for (UUID uuid : view.listOrEmpty("player_list", UUIDUtil.AUTHLIB_CODEC)) {
			this.playerList.add(uuid);
		}

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	public static void tick(Level world, BlockPos pos, BlockState state, PlayerDetectorBlockEntity playerDetectorBlockEntity) {
		if (!world.isClientSide() && playerDetectorBlockEntity.enableTicking() && world.getGameTime() % Math.max(1L, ScriptBlocksConfigs.SERVER_CONFIG.player_detector_block_tick_interval) == 0L) {
			if (playerDetectorBlockEntity.calculateAreaBox || playerDetectorBlockEntity.area == null) {
				BlockPos areaPositionOffset = playerDetectorBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = playerDetectorBlockEntity.areaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				playerDetectorBlockEntity.area = new AABB(areaStart, areaEnd);
				playerDetectorBlockEntity.calculateAreaBox = false;
			}

			List<Player> detectedPlayerList = world.getEntitiesOfClass(Player.class, playerDetectorBlockEntity.area);
			List<UUID> detectedPlayersUuidList = new ArrayList<>();
			for (Player player : detectedPlayerList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersUuidList.add(player.getUUID());
				}
			}
			ArrayList<UUID> newCurrentPlayerUUIDList = new ArrayList<>();
			List<UUID> enteringPlayersUuidList = new ArrayList<>();
			List<UUID> leavingPlayersUuidList = new ArrayList<>(playerDetectorBlockEntity.playerList);

			Player playerEntity;

			for (UUID uuid : detectedPlayersUuidList) {
				playerEntity = world.getPlayerByUUID(uuid);

				if (playerEntity != null) {
					if (playerDetectorBlockEntity.playerList.contains(uuid)) {
						newCurrentPlayerUUIDList.add(uuid);
						leavingPlayersUuidList.remove(uuid);
					} else {
						enteringPlayersUuidList.add(uuid);
						newCurrentPlayerUUIDList.add(uuid);
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

			playerDetectorBlockEntity.setChanged();
			world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
		}
	}

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	private void triggerBlockOnEntering() {
		if (this.level != null && !this.onEnteringTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringTriggeredBlock.getLeft()));
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
		if (this.level != null && !this.onLeavingTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onLeavingTriggeredBlock.getLeft()));
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
		if (this.level != null && !this.onTriggeringTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onTriggeringTriggeredBlock.getLeft()));
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
		if (this.level != null && !this.onEnteringUUIDListHandler.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnLeaving(List<UUID> uuidList) {
		if (this.level != null && !this.onLeavingUUIDListHandler.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onLeavingUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnTriggering(List<UUID> uuidList) {
		if (this.level != null && !this.onTriggeringUUIDListHandler.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onTriggeringUUIDListHandler));
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
		if (this.getLevel() != null) {
			return;
		}
		if (this.calculateAreaBox || this.area == null) {
			BlockPos areaPositionOffset = this.areaPositionOffset;
			Vec3i areaDimensions = this.areaDimensions;
			Vec3 areaStart = new Vec3(worldPosition.getX() + areaPositionOffset.getX(), worldPosition.getY() + areaPositionOffset.getY(), worldPosition.getZ() + areaPositionOffset.getZ());
			Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
			this.area = new AABB(areaStart, areaEnd);
			this.calculateAreaBox = false;
		}

		List<Player> currentPlayerList = this.getLevel().getEntitiesOfClass(Player.class, this.area);
		List<UUID> currentPlayersUuidList = new ArrayList<>();
		for (Player player : currentPlayerList) {
			if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
				currentPlayersUuidList.add(player.getUUID());
			}
		}

		if (!currentPlayersUuidList.isEmpty()) {
			this.sendPlayerUUIDListOnTriggering(currentPlayersUuidList);
			this.triggerBlockOnTriggering();
		}
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		if (this.getLevel() == null) {
			return new ArrayList<>();
		}

		if (this.calculateAreaBox || this.area == null) {
			BlockPos areaPositionOffset = this.areaPositionOffset;
			Vec3i areaDimensions = this.areaDimensions;
			Vec3 areaStart = new Vec3(worldPosition.getX() + areaPositionOffset.getX(), worldPosition.getY() + areaPositionOffset.getY(), worldPosition.getZ() + areaPositionOffset.getZ());
			Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
			this.area = new AABB(areaStart, areaEnd);
			this.calculateAreaBox = false;
		}

		List<Player> currentPlayerList = this.getLevel().getEntitiesOfClass(Player.class, this.area);
		List<UUID> currentPlayersUuidList = new ArrayList<>();
		for (Player player : currentPlayerList) {
			if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
				currentPlayersUuidList.add(player.getUUID());
			}
		}

		return currentPlayersUuidList;
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

	}

	@Override
	public void reset() {
		this.playerList.clear();
		this.area = null;
		this.calculateAreaBox = true;
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), blockRotation));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), blockRotation));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), blockRotation));

				this.onEnteringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringUUIDListHandler, blockRotation);
				this.onLeavingUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingUUIDListHandler, blockRotation);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringUUIDListHandler, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, Mirror.FRONT_BACK);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, Mirror.FRONT_BACK);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, Mirror.LEFT_RIGHT);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, Mirror.LEFT_RIGHT);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
