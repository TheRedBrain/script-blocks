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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDetectorBlockEntity extends RotatedBlockEntity implements HandlesUUIDList, Triggerable {

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
	protected void writeData(WriteView view) {

		view.putBoolean("enable_ticking", this.enableTicking);

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.put("area", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringTriggeredBlock.getRight());
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringUUIDListHandler);
		view.put("on_entering", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.onLeavingTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onLeavingTriggeredBlock.getRight());
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onLeavingUUIDListHandler);
		view.put("on_leaving", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.onTriggeringTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onTriggeringTriggeredBlock.getRight());
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onTriggeringUUIDListHandler);
		view.put("on_triggering", NbtCompound.CODEC, nbtCompound.copy());

		WriteView.ListAppender<UUID> listAppender = view.getListAppender("player_list", Uuids.CODEC);
		for (UUID uuid : this.playerList) {
			listAppender.add(uuid);
		}

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		this.enableTicking = view.getBoolean("enable_ticking", false);

		NbtCompound nbtCompound = view.read("area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showArea = nbtCompound.getBoolean("visible", false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onEnteringTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		this.onEnteringUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);


		nbtCompound = view.read("on_leaving", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onLeavingTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		this.onLeavingUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_triggering", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onTriggeringTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		this.onTriggeringUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.playerList.clear();
		for (UUID uuid : view.getTypedListView("player_list", Uuids.CODEC)) {
			this.playerList.add(uuid);
		}

		super.readData(view);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public static void tick(World world, BlockPos pos, BlockState state, PlayerDetectorBlockEntity playerDetectorBlockEntity) {
		if (!world.isClient() && playerDetectorBlockEntity.enableTicking() && world.getTime() % Math.max(1L, ScriptBlocksConfigs.SERVER_CONFIG.player_detector_block_tick_interval) == 0L) {
			if (playerDetectorBlockEntity.calculateAreaBox || playerDetectorBlockEntity.area == null) {
				BlockPos areaPositionOffset = playerDetectorBlockEntity.areaPositionOffset;
				Vec3i areaDimensions = playerDetectorBlockEntity.areaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				playerDetectorBlockEntity.area = new Box(areaStart, areaEnd);
				playerDetectorBlockEntity.calculateAreaBox = false;
			}

			List<PlayerEntity> detectedPlayerList = world.getNonSpectatingEntities(PlayerEntity.class, playerDetectorBlockEntity.area);
			List<UUID> detectedPlayersUuidList = new ArrayList<>();
			for (PlayerEntity player : detectedPlayerList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersUuidList.add(player.getUuid());
				}
			}
			ArrayList<UUID> newCurrentPlayerUUIDList = new ArrayList<>();
			List<UUID> enteringPlayersUuidList = new ArrayList<>();
			List<UUID> leavingPlayersUuidList = new ArrayList<>(playerDetectorBlockEntity.playerList);

			PlayerEntity playerEntity;

			for (UUID uuid : detectedPlayersUuidList) {
				playerEntity = world.getPlayerByUuid(uuid);

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

			playerDetectorBlockEntity.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
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
		if (this.getWorld() != null) {
			return;
		}
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

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		if (this.getWorld() == null) {
			return new ArrayList<>();
		}

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
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), blockRotation));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), blockRotation));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), blockRotation));

				this.onEnteringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringUUIDListHandler, blockRotation);
				this.onLeavingUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onLeavingUUIDListHandler, blockRotation);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onTriggeringUUIDListHandler, blockRotation);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, BlockMirror.FRONT_BACK);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, BlockMirror.FRONT_BACK);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.onEnteringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onLeavingTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onTriggeringTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.onEnteringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringUUIDListHandler, BlockMirror.LEFT_RIGHT);
				this.onLeavingUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onLeavingUUIDListHandler, BlockMirror.LEFT_RIGHT);
				this.onTriggeringUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onTriggeringUUIDListHandler, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
