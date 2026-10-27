package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class UUIDListSavingBlockEntity extends BlockEntity implements HandlesUUIDList {

	private final Set<UUID> uuidList = new HashSet<>();

	public UUIDListSavingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_SAVING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<UUID> listAppender = view.list("uuidList", UUIDUtil.AUTHLIB_CODEC);
		for (UUID uuid : this.uuidList) {
			listAppender.add(uuid);
		}

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.uuidList.clear();
		for (UUID uuid : view.listOrEmpty("uuid_list", UUIDUtil.AUTHLIB_CODEC)) {
			this.uuidList.add(uuid);
		}

		super.loadAdditional(view);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		List<UUID> list = this.uuidList.stream().toList();
		if (remove) {
			this.reset();
		}
		return list;
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {

		if (remove) {
			list.forEach(this.uuidList::remove);
		} else {
			this.uuidList.addAll(list);
		}

		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	@Override
	public void reset() {
		this.uuidList.clear();
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
