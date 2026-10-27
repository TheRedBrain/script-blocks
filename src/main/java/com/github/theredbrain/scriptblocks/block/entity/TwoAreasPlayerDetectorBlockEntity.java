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

public class TwoAreasPlayerDetectorBlockEntity extends RotatedBlockEntity implements Resetable {

	private boolean calculateFirstAreaBox = true;
	private AABB firstArea = null;
	private boolean showFirstArea = false;
	private Vec3i firstAreaDimensions = Vec3i.ZERO;
	private BlockPos firstAreaPositionOffset = BlockPos.ZERO;

	private boolean calculateSecondAreaBox = true;
	private AABB secondArea = null;
	private boolean showSecondArea = false;
	private Vec3i secondAreaDimensions = Vec3i.ZERO;
	private BlockPos secondAreaPositionOffset = BlockPos.ZERO;

	private MutablePair<BlockPos, Boolean> onEnteringFirstAreaTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private MutablePair<BlockPos, Boolean> onEnteringSecondAreaTriggeredBlock = new MutablePair<>(BlockPos.ZERO, false);
	private BlockPos onEnteringFirstAreaUUIDListHandler = BlockPos.ZERO;
	private BlockPos onEnteringSecondAreaUUIDListHandler = BlockPos.ZERO;

	private final ArrayList<UUID> firstPlayerList = new ArrayList<>();
	private final ArrayList<UUID> secondPlayerList = new ArrayList<>();

	public TwoAreasPlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TWO_AREAS_PLAYER_DETECTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showFirstArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.firstAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.firstAreaDimensions));
		view.store("first_area", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showSecondArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.secondAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.secondAreaDimensions));
		view.store("second_area", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringFirstAreaTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringFirstAreaTriggeredBlock.getRight());
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringFirstAreaUUIDListHandler);
		view.store("on_entering_first_area", CompoundTag.CODEC, nbtCompound.copy());

		nbtCompound = new CompoundTag();
		nbtCompound.store("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringSecondAreaTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringSecondAreaTriggeredBlock.getRight());
		nbtCompound.store("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringSecondAreaUUIDListHandler);
		view.store("on_entering_second_area", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<UUID> listAppender = view.list("first_player_list", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.firstPlayerList) {
			listAppender.add(uuid);
		}

		ValueOutput.TypedOutputList<UUID> listAppender1 = view.list("second_player_list", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.secondPlayerList) {
			listAppender1.add(uuid);
		}

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("first_area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showFirstArea = nbtCompound.getBooleanOr("visible", false);

		this.firstAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.firstAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("second_area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showSecondArea = nbtCompound.getBooleanOr("visible", false);

		this.secondAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.secondAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering_first_area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onEnteringFirstAreaTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		this.onEnteringFirstAreaUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering_second_area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.onEnteringSecondAreaTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.read("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBooleanOr("triggered_block_resets", false)
		);

		this.onEnteringSecondAreaUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.firstPlayerList.clear();
		for (UUID uuid : view.listOrEmpty("first_player_list", UUIDUtil.AUTHLIB_CODEC)) {
			this.firstPlayerList.add(uuid);
		}

		this.secondPlayerList.clear();
		for (UUID uuid : view.listOrEmpty("second_player_list", UUIDUtil.AUTHLIB_CODEC)) {
			this.secondPlayerList.add(uuid);
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

	public static void tick(Level world, BlockPos pos, BlockState state, TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		if (!world.isClientSide() && world.getGameTime() % Math.max(1L, ScriptBlocksConfigs.SERVER_CONFIG.two_areas_player_detector_block_tick_interval) == 0L) {
			if (twoAreasPlayerDetectorBlockEntity.calculateFirstAreaBox || twoAreasPlayerDetectorBlockEntity.firstArea == null) {
				BlockPos areaPositionOffset = twoAreasPlayerDetectorBlockEntity.firstAreaPositionOffset;
				Vec3i areaDimensions = twoAreasPlayerDetectorBlockEntity.firstAreaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				twoAreasPlayerDetectorBlockEntity.firstArea = new AABB(areaStart, areaEnd);
				twoAreasPlayerDetectorBlockEntity.calculateFirstAreaBox = false;
			}
			if (twoAreasPlayerDetectorBlockEntity.calculateSecondAreaBox || twoAreasPlayerDetectorBlockEntity.secondArea == null) {
				BlockPos areaPositionOffset = twoAreasPlayerDetectorBlockEntity.secondAreaPositionOffset;
				Vec3i areaDimensions = twoAreasPlayerDetectorBlockEntity.secondAreaDimensions;
				Vec3 areaStart = new Vec3(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3 areaEnd = new Vec3(areaStart.x() + areaDimensions.getX(), areaStart.y() + areaDimensions.getY(), areaStart.z() + areaDimensions.getZ());
				twoAreasPlayerDetectorBlockEntity.secondArea = new AABB(areaStart, areaEnd);
				twoAreasPlayerDetectorBlockEntity.calculateSecondAreaBox = false;
			}

			List<Player> detectedPlayersInFirstAreaList = world.getEntitiesOfClass(Player.class, twoAreasPlayerDetectorBlockEntity.firstArea);
			List<Player> detectedPlayersInSecondAreaList = world.getEntitiesOfClass(Player.class, twoAreasPlayerDetectorBlockEntity.secondArea);

			List<UUID> detectedPlayersInFirstAreaUuidList = new ArrayList<>();
			for (Player player : detectedPlayersInFirstAreaList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersInFirstAreaUuidList.add(player.getUUID());
				}
			}

			List<UUID> detectedPlayersInSecondAreaUuidList = new ArrayList<>();
			for (Player player : detectedPlayersInSecondAreaList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersInSecondAreaUuidList.add(player.getUUID());
				}
			}

			ArrayList<UUID> newFirstPlayerUUIDList = new ArrayList<>();
			ArrayList<UUID> newSecondPlayerUUIDList = new ArrayList<>();
			List<UUID> enteringFirstAreaPlayersUuidList = new ArrayList<>();
			List<UUID> enteringSecondAreaPlayersUuidList = new ArrayList<>();

			Player playerEntity;

			for (UUID uuid : detectedPlayersInFirstAreaUuidList) {
				playerEntity = world.getPlayerByUUID(uuid);

				if (playerEntity != null) {
					if (!twoAreasPlayerDetectorBlockEntity.firstPlayerList.contains(uuid) && twoAreasPlayerDetectorBlockEntity.secondPlayerList.contains(uuid)) {
						enteringFirstAreaPlayersUuidList.add(uuid);
					}
					newFirstPlayerUUIDList.add(uuid);
				}
			}

			for (UUID uuid : detectedPlayersInSecondAreaUuidList) {
				playerEntity = world.getPlayerByUUID(uuid);

				if (playerEntity != null) {
					if (!twoAreasPlayerDetectorBlockEntity.secondPlayerList.contains(uuid) && twoAreasPlayerDetectorBlockEntity.firstPlayerList.contains(uuid)) {
						enteringSecondAreaPlayersUuidList.add(uuid);
					}
					newSecondPlayerUUIDList.add(uuid);
				}
			}

			if (!enteringFirstAreaPlayersUuidList.isEmpty()) {
				twoAreasPlayerDetectorBlockEntity.sendPlayerUUIDListOnEnteringFirstArea(enteringFirstAreaPlayersUuidList);
				twoAreasPlayerDetectorBlockEntity.triggerBlockOnEnteringFirstArea();
			}
			if (!enteringSecondAreaPlayersUuidList.isEmpty()) {
				twoAreasPlayerDetectorBlockEntity.sendPlayerUUIDListOnEnteringSecondArea(enteringSecondAreaPlayersUuidList);
				twoAreasPlayerDetectorBlockEntity.triggerBlockOnEnteringSecondArea();
			}

			twoAreasPlayerDetectorBlockEntity.firstPlayerList.clear();
			twoAreasPlayerDetectorBlockEntity.firstPlayerList.addAll(newFirstPlayerUUIDList);

			twoAreasPlayerDetectorBlockEntity.secondPlayerList.clear();
			twoAreasPlayerDetectorBlockEntity.secondPlayerList.addAll(newSecondPlayerUUIDList);

			twoAreasPlayerDetectorBlockEntity.setChanged();
			world.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
		}
	}

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	private void triggerBlockOnEnteringFirstArea() {
		if (this.level != null && !this.onEnteringFirstAreaTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringFirstAreaTriggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.onEnteringFirstAreaTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	private void triggerBlockOnEnteringSecondArea() {
		if (this.level != null && !this.onEnteringSecondAreaTriggeredBlock.getLeft().equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringSecondAreaTriggeredBlock.getLeft()));
			if (blockEntity != this) {
				boolean triggeredBlockResets = this.onEnteringSecondAreaTriggeredBlock.getRight();
				if (triggeredBlockResets && blockEntity instanceof Resetable resetable) {
					resetable.reset();
				} else if (!triggeredBlockResets && blockEntity instanceof Triggerable triggerable) {
					triggerable.trigger();
				}
			}
		}
	}

	public void sendPlayerUUIDListOnEnteringFirstArea(List<UUID> uuidList) {
		if (this.level != null && !this.onEnteringFirstAreaUUIDListHandler.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringFirstAreaUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnEnteringSecondArea(List<UUID> uuidList) {
		if (this.level != null && !this.onEnteringSecondAreaUUIDListHandler.equals(BlockPos.ZERO)) {
			BlockEntity blockEntity = level.getBlockEntity(getActualOffsetBlockPosition(this.worldPosition, this.onEnteringSecondAreaUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	// region --- getter & setter ---
	public boolean showFirstArea() {
		return this.showFirstArea;
	}

	public void setShowFirstArea(boolean showFirstArea) {
		this.showFirstArea = showFirstArea;
	}

	public boolean showSecondArea() {
		return this.showSecondArea;
	}

	public void setShowSecondArea(boolean showSecondArea) {
		this.showSecondArea = showSecondArea;
	}

	public Vec3i getFirstAreaDimensions() {
		return this.firstAreaDimensions;
	}

	public void setFirstAreaDimensions(Vec3i firstAreaDimensions) {
		this.firstAreaDimensions = firstAreaDimensions;
		this.calculateFirstAreaBox = true;
	}

	public BlockPos getFirstAreaPositionOffset() {
		return this.firstAreaPositionOffset;
	}

	public void setFirstAreaPositionOffset(BlockPos firstAreaPositionOffset) {
		this.firstAreaPositionOffset = firstAreaPositionOffset;
		this.calculateFirstAreaBox = true;
	}

	public Vec3i getSecondAreaDimensions() {
		return this.secondAreaDimensions;
	}

	public void setSecondAreaDimensions(Vec3i secondAreaDimensions) {
		this.secondAreaDimensions = secondAreaDimensions;
		this.calculateSecondAreaBox = true;
	}

	public BlockPos getSecondAreaPositionOffset() {
		return this.secondAreaPositionOffset;
	}

	public void setSecondAreaPositionOffset(BlockPos secondAreaPositionOffset) {
		this.secondAreaPositionOffset = secondAreaPositionOffset;
		this.calculateSecondAreaBox = true;
	}

	public MutablePair<BlockPos, Boolean> getOnEnteringFirstAreaTriggeredBlock() {
		return this.onEnteringFirstAreaTriggeredBlock;
	}

	public void setOnEnteringFirstAreaTriggeredBlock(MutablePair<BlockPos, Boolean> onEnteringFirstAreaTriggeredBlock) {
		this.onEnteringFirstAreaTriggeredBlock = onEnteringFirstAreaTriggeredBlock;
	}

	public MutablePair<BlockPos, Boolean> getOnEnteringSecondAreaTriggeredBlock() {
		return this.onEnteringSecondAreaTriggeredBlock;
	}

	public void setOnEnteringSecondAreaTriggeredBlock(MutablePair<BlockPos, Boolean> onEnteringSecondAreaTriggeredBlock) {
		this.onEnteringSecondAreaTriggeredBlock = onEnteringSecondAreaTriggeredBlock;
	}

	public BlockPos getOnEnteringFirstAreaUUIDListHandler() {
		return this.onEnteringFirstAreaUUIDListHandler;
	}

	public void setOnEnteringFirstAreaUUIDListHandler(BlockPos onEnteringFirstAreaUUIDListHandler) {
		this.onEnteringFirstAreaUUIDListHandler = onEnteringFirstAreaUUIDListHandler;
	}

	public BlockPos getOnEnteringSecondAreaUUIDListHandler() {
		return this.onEnteringSecondAreaUUIDListHandler;
	}

	public void setOnEnteringSecondAreaUUIDListHandler(BlockPos onEnteringSecondAreaUUIDListHandler) {
		this.onEnteringSecondAreaUUIDListHandler = onEnteringSecondAreaUUIDListHandler;
	}
	// endregion --- getter & setter ---

	@Override
	public void reset() {
		this.firstPlayerList.clear();
		this.secondPlayerList.clear();
		this.firstArea = null;
		this.secondArea = null;
		this.calculateFirstAreaBox = true;
		this.calculateSecondAreaBox = true;
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

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.firstAreaPositionOffset, this.firstAreaDimensions, blockRotation);
				this.firstAreaPositionOffset = offsetArea.getLeft();
				this.firstAreaDimensions = offsetArea.getRight();

				offsetArea = BlockRotationUtils.rotateOffsetArea(this.secondAreaPositionOffset, this.secondAreaDimensions, blockRotation);
				this.secondAreaPositionOffset = offsetArea.getLeft();
				this.secondAreaDimensions = offsetArea.getRight();

				this.onEnteringFirstAreaTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringFirstAreaTriggeredBlock.getLeft(), blockRotation));
				this.onEnteringSecondAreaTriggeredBlock.setLeft(BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringSecondAreaTriggeredBlock.getLeft(), blockRotation));

				this.onEnteringFirstAreaUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringFirstAreaUUIDListHandler, blockRotation);
				this.onEnteringSecondAreaUUIDListHandler = BlockRotationUtils.rotateOffsetBlockPos(this.onEnteringSecondAreaUUIDListHandler, blockRotation);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.firstAreaPositionOffset, this.firstAreaDimensions, Mirror.FRONT_BACK);
				this.firstAreaPositionOffset = offsetArea.getLeft();
				this.firstAreaDimensions = offsetArea.getRight();

				offsetArea = BlockRotationUtils.mirrorOffsetArea(this.secondAreaPositionOffset, this.secondAreaDimensions, Mirror.FRONT_BACK);
				this.secondAreaPositionOffset = offsetArea.getLeft();
				this.secondAreaDimensions = offsetArea.getRight();

				this.onEnteringFirstAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaTriggeredBlock.getLeft(), Mirror.FRONT_BACK));
				this.onEnteringSecondAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaTriggeredBlock.getLeft(), Mirror.FRONT_BACK));

				this.onEnteringFirstAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaUUIDListHandler, Mirror.FRONT_BACK);
				this.onEnteringSecondAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaUUIDListHandler, Mirror.FRONT_BACK);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.firstAreaPositionOffset, this.firstAreaDimensions, Mirror.LEFT_RIGHT);
				this.firstAreaPositionOffset = offsetArea.getLeft();
				this.firstAreaDimensions = offsetArea.getRight();

				offsetArea = BlockRotationUtils.mirrorOffsetArea(this.secondAreaPositionOffset, this.secondAreaDimensions, Mirror.LEFT_RIGHT);
				this.secondAreaPositionOffset = offsetArea.getLeft();
				this.secondAreaDimensions = offsetArea.getRight();

				this.onEnteringFirstAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));
				this.onEnteringSecondAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaTriggeredBlock.getLeft(), Mirror.LEFT_RIGHT));

				this.onEnteringFirstAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaUUIDListHandler, Mirror.LEFT_RIGHT);
				this.onEnteringSecondAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaUUIDListHandler, Mirror.LEFT_RIGHT);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
