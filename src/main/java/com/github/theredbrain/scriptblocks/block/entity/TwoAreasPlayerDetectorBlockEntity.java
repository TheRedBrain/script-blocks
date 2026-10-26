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

public class TwoAreasPlayerDetectorBlockEntity extends RotatedBlockEntity implements Resetable {

	private boolean calculateFirstAreaBox = true;
	private Box firstArea = null;
	private boolean showFirstArea = false;
	private Vec3i firstAreaDimensions = Vec3i.ZERO;
	private BlockPos firstAreaPositionOffset = BlockPos.ORIGIN;

	private boolean calculateSecondAreaBox = true;
	private Box secondArea = null;
	private boolean showSecondArea = false;
	private Vec3i secondAreaDimensions = Vec3i.ZERO;
	private BlockPos secondAreaPositionOffset = BlockPos.ORIGIN;

	private MutablePair<BlockPos, Boolean> onEnteringFirstAreaTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private MutablePair<BlockPos, Boolean> onEnteringSecondAreaTriggeredBlock = new MutablePair<>(BlockPos.ORIGIN, false);
	private BlockPos onEnteringFirstAreaUUIDListHandler = BlockPos.ORIGIN;
	private BlockPos onEnteringSecondAreaUUIDListHandler = BlockPos.ORIGIN;

	private final ArrayList<UUID> firstPlayerList = new ArrayList<>();
	private final ArrayList<UUID> secondPlayerList = new ArrayList<>();

	public TwoAreasPlayerDetectorBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TWO_AREAS_PLAYER_DETECTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showFirstArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.firstAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.firstAreaDimensions));
		view.put("first_area", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showSecondArea);
		nbtCompound.put("position_offset", BlockPos.CODEC, this.secondAreaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.secondAreaDimensions));
		view.put("second_area", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringFirstAreaTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringFirstAreaTriggeredBlock.getRight());
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringFirstAreaUUIDListHandler);
		view.put("on_entering_first_area", NbtCompound.CODEC, nbtCompound.copy());

		nbtCompound = new NbtCompound();
		nbtCompound.put("triggered_block_position_offset", BlockPos.CODEC, this.onEnteringSecondAreaTriggeredBlock.getLeft());
		nbtCompound.putBoolean("triggered_block_resets", this.onEnteringSecondAreaTriggeredBlock.getRight());
		nbtCompound.put("uuid_list_handler_position_offset", BlockPos.CODEC, this.onEnteringSecondAreaUUIDListHandler);
		view.put("on_entering_second_area", NbtCompound.CODEC, nbtCompound.copy());

		WriteView.ListAppender<UUID> listAppender = view.getListAppender("first_player_list", Uuids.CODEC);
		for (UUID uuid : this.firstPlayerList) {
			listAppender.add(uuid);
		}

		WriteView.ListAppender<UUID> listAppender1 = view.getListAppender("second_player_list", Uuids.CODEC);
		for (UUID uuid : this.secondPlayerList) {
			listAppender1.add(uuid);
		}

		super.writeData(view);

	}

	@Override
	protected void readData(ReadView view) {

		NbtCompound nbtCompound = view.read("first_area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showFirstArea = nbtCompound.getBoolean("visible", false);

		this.firstAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.firstAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("second_area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.showSecondArea = nbtCompound.getBoolean("visible", false);

		this.secondAreaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.secondAreaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering_first_area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onEnteringFirstAreaTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		this.onEnteringFirstAreaUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		nbtCompound = view.read("on_entering_second_area", NbtCompound.CODEC).orElseGet(NbtCompound::new);
		this.onEnteringSecondAreaTriggeredBlock = new MutablePair<>(
				BlockPos.max(
						BlockPos.min(
								nbtCompound.get("triggered_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
								new BlockPos(48, 48, 48)
						),
						new BlockPos(-48, -48, -48)
				),
				nbtCompound.getBoolean("triggered_block_resets", false)
		);

		this.onEnteringSecondAreaUUIDListHandler = BlockPos.max(
				BlockPos.min(
						nbtCompound.get("uuid_list_handler_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.firstPlayerList.clear();
		for (UUID uuid : view.getTypedListView("first_player_list", Uuids.CODEC)) {
			this.firstPlayerList.add(uuid);
		}

		this.secondPlayerList.clear();
		for (UUID uuid : view.getTypedListView("second_player_list", Uuids.CODEC)) {
			this.secondPlayerList.add(uuid);
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

	public static void tick(World world, BlockPos pos, BlockState state, TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		if (!world.isClient() && world.getTime() % Math.max(1L, ScriptBlocksConfigs.SERVER_CONFIG.two_areas_player_detector_block_tick_interval) == 0L) {
			if (twoAreasPlayerDetectorBlockEntity.calculateFirstAreaBox || twoAreasPlayerDetectorBlockEntity.firstArea == null) {
				BlockPos areaPositionOffset = twoAreasPlayerDetectorBlockEntity.firstAreaPositionOffset;
				Vec3i areaDimensions = twoAreasPlayerDetectorBlockEntity.firstAreaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				twoAreasPlayerDetectorBlockEntity.firstArea = new Box(areaStart, areaEnd);
				twoAreasPlayerDetectorBlockEntity.calculateFirstAreaBox = false;
			}
			if (twoAreasPlayerDetectorBlockEntity.calculateSecondAreaBox || twoAreasPlayerDetectorBlockEntity.secondArea == null) {
				BlockPos areaPositionOffset = twoAreasPlayerDetectorBlockEntity.secondAreaPositionOffset;
				Vec3i areaDimensions = twoAreasPlayerDetectorBlockEntity.secondAreaDimensions;
				Vec3d areaStart = new Vec3d(pos.getX() + areaPositionOffset.getX(), pos.getY() + areaPositionOffset.getY(), pos.getZ() + areaPositionOffset.getZ());
				Vec3d areaEnd = new Vec3d(areaStart.getX() + areaDimensions.getX(), areaStart.getY() + areaDimensions.getY(), areaStart.getZ() + areaDimensions.getZ());
				twoAreasPlayerDetectorBlockEntity.secondArea = new Box(areaStart, areaEnd);
				twoAreasPlayerDetectorBlockEntity.calculateSecondAreaBox = false;
			}

			List<PlayerEntity> detectedPlayersInFirstAreaList = world.getNonSpectatingEntities(PlayerEntity.class, twoAreasPlayerDetectorBlockEntity.firstArea);
			List<PlayerEntity> detectedPlayersInSecondAreaList = world.getNonSpectatingEntities(PlayerEntity.class, twoAreasPlayerDetectorBlockEntity.secondArea);

			List<UUID> detectedPlayersInFirstAreaUuidList = new ArrayList<>();
			for (PlayerEntity player : detectedPlayersInFirstAreaList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersInFirstAreaUuidList.add(player.getUuid());
				}
			}

			List<UUID> detectedPlayersInSecondAreaUuidList = new ArrayList<>();
			for (PlayerEntity player : detectedPlayersInSecondAreaList) {
				if (!player.isCreative() || ScriptBlocksConfigs.SERVER_CONFIG.enable_creative_player_detection) {
					detectedPlayersInSecondAreaUuidList.add(player.getUuid());
				}
			}

			ArrayList<UUID> newFirstPlayerUUIDList = new ArrayList<>();
			ArrayList<UUID> newSecondPlayerUUIDList = new ArrayList<>();
			List<UUID> enteringFirstAreaPlayersUuidList = new ArrayList<>();
			List<UUID> enteringSecondAreaPlayersUuidList = new ArrayList<>();

			PlayerEntity playerEntity;

			for (UUID uuid : detectedPlayersInFirstAreaUuidList) {
				playerEntity = world.getPlayerByUuid(uuid);

				if (playerEntity != null) {
					if (!twoAreasPlayerDetectorBlockEntity.firstPlayerList.contains(uuid) && twoAreasPlayerDetectorBlockEntity.secondPlayerList.contains(uuid)) {
						enteringFirstAreaPlayersUuidList.add(uuid);
					}
					newFirstPlayerUUIDList.add(uuid);
				}
			}

			for (UUID uuid : detectedPlayersInSecondAreaUuidList) {
				playerEntity = world.getPlayerByUuid(uuid);

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

			twoAreasPlayerDetectorBlockEntity.markDirty();
			world.updateListeners(pos, state, state, Block.NOTIFY_ALL);
		}
	}

	public static BlockPos getActualOffsetBlockPosition(BlockPos originalBlockPos, BlockPos offsetBlockPos) {
		return new BlockPos(originalBlockPos.getX() + offsetBlockPos.getX(), originalBlockPos.getY() + offsetBlockPos.getY(), originalBlockPos.getZ() + offsetBlockPos.getZ());
	}

	private void triggerBlockOnEnteringFirstArea() {
		if (this.world != null && !this.onEnteringFirstAreaTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringFirstAreaTriggeredBlock.getLeft()));
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
		if (this.world != null && !this.onEnteringSecondAreaTriggeredBlock.getLeft().equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringSecondAreaTriggeredBlock.getLeft()));
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
		if (this.world != null && !this.onEnteringFirstAreaUUIDListHandler.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringFirstAreaUUIDListHandler));
			if (blockEntity != this && blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(uuidList, false);
			}
		}
	}

	public void sendPlayerUUIDListOnEnteringSecondArea(List<UUID> uuidList) {
		if (this.world != null && !this.onEnteringSecondAreaUUIDListHandler.equals(BlockPos.ORIGIN)) {
			BlockEntity blockEntity = world.getBlockEntity(getActualOffsetBlockPosition(this.pos, this.onEnteringSecondAreaUUIDListHandler));
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

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.firstAreaPositionOffset, this.firstAreaDimensions, BlockMirror.FRONT_BACK);
				this.firstAreaPositionOffset = offsetArea.getLeft();
				this.firstAreaDimensions = offsetArea.getRight();

				offsetArea = BlockRotationUtils.mirrorOffsetArea(this.secondAreaPositionOffset, this.secondAreaDimensions, BlockMirror.FRONT_BACK);
				this.secondAreaPositionOffset = offsetArea.getLeft();
				this.secondAreaDimensions = offsetArea.getRight();

				this.onEnteringFirstAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));
				this.onEnteringSecondAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaTriggeredBlock.getLeft(), BlockMirror.FRONT_BACK));

				this.onEnteringFirstAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaUUIDListHandler, BlockMirror.FRONT_BACK);
				this.onEnteringSecondAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaUUIDListHandler, BlockMirror.FRONT_BACK);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.firstAreaPositionOffset, this.firstAreaDimensions, BlockMirror.LEFT_RIGHT);
				this.firstAreaPositionOffset = offsetArea.getLeft();
				this.firstAreaDimensions = offsetArea.getRight();

				offsetArea = BlockRotationUtils.mirrorOffsetArea(this.secondAreaPositionOffset, this.secondAreaDimensions, BlockMirror.LEFT_RIGHT);
				this.secondAreaPositionOffset = offsetArea.getLeft();
				this.secondAreaDimensions = offsetArea.getRight();

				this.onEnteringFirstAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));
				this.onEnteringSecondAreaTriggeredBlock.setLeft(BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaTriggeredBlock.getLeft(), BlockMirror.LEFT_RIGHT));

				this.onEnteringFirstAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringFirstAreaUUIDListHandler, BlockMirror.LEFT_RIGHT);
				this.onEnteringSecondAreaUUIDListHandler = BlockRotationUtils.mirrorOffsetBlockPos(this.onEnteringSecondAreaUUIDListHandler, BlockMirror.LEFT_RIGHT);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
