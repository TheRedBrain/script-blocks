package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class UUIDListRelayBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private final List<BlockPos> uuidListHandlers = new ArrayList<>();

	public UUIDListRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("uuid_list_handlers", CompoundTag.CODEC);
		for (BlockPos blockPos : this.uuidListHandlers) {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, blockPos);
			listAppender.add(nbtCompound.copy());
		}

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.uuidListHandlers.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("uuid_list_handlers", CompoundTag.CODEC)) {
			this.uuidListHandlers.add(
					BlockPos.max(
							BlockPos.min(
									nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
									new BlockPos(48, 48, 48)
							),
							new BlockPos(-48, -48, -48)
					)
			);
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

	// region --- getter & setter ---
	public List<BlockPos> getUuidListHandlers() {
		return this.uuidListHandlers;
	}

	public void setUuidListHandlers(List<BlockPos> uuidListHandlers) {
		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(uuidListHandlers);
	}
	// endregion --- getter & setter ---

	public BlockPos getActualUUIDListHandlerPosition(BlockPos uuidListHandler) {
		return new BlockPos(this.worldPosition.getX() + uuidListHandler.getX(), this.worldPosition.getY() + uuidListHandler.getY(), this.worldPosition.getZ() + uuidListHandler.getZ());
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		if (this.getLevel() == null) {
			return new ArrayList<>();
		}
		BlockEntity blockEntity;
		Set<UUID> newUuidList = new HashSet<>();
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getLevel().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				continue;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				newUuidList.addAll(handlesUUIDList.supplyUUIDList(remove));
			}
		}
		return newUuidList.stream().toList();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.getLevel() == null) {
			return;
		}
		BlockEntity blockEntity;
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getLevel().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				return;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(list, remove);
			}
		}
	}

	@Override
	public void reset() {
		if (this.getLevel() == null) {
			return;
		}
		BlockEntity blockEntity;
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getLevel().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				continue;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.rotateOffsetBlockPos(uuidListHandler, blockRotation));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.mirrorOffsetBlockPos(uuidListHandler, Mirror.FRONT_BACK));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.mirrorOffsetBlockPos(uuidListHandler, Mirror.LEFT_RIGHT));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

}
