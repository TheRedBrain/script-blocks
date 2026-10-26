package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UUIDListSavingBlockEntity extends BlockEntity implements HandlesUUIDList {

	private final Set<UUID> uuidList = new HashSet<>();

	public UUIDListSavingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_SAVING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		WriteView.ListAppender<UUID> listAppender = view.getListAppender("uuidList", Uuids.CODEC);
		for (UUID uuid : this.uuidList) {
			listAppender.add(uuid);
		}

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.uuidList.clear();
		for (UUID uuid : view.getTypedListView("uuid_list", Uuids.CODEC)) {
			this.uuidList.add(uuid);
		}

		super.readData(view);
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
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

		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}

	@Override
	public void reset() {
		this.uuidList.clear();
		this.markDirty();
		if (this.world != null) {
			BlockState blockState = this.world.getBlockState(this.pos);
			this.world.updateListeners(this.pos, blockState, blockState, Block.NOTIFY_ALL);
		}
	}
}
