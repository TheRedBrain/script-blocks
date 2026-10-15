package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
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
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtList nbtList = new NbtList();
		for (UUID uuid : this.uuidList) {
			nbtList.add(NbtHelper.fromUuid(uuid));
		}
		nbt.put("uuid_list", nbtList);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.uuidList.clear();
		if (nbt.contains("uuid_list_size")) {
			int listSize = nbt.getInt("uuid_list_size");
			for (int i = 0; i < listSize; i++) {
				if (nbt.containsUuid("uuid_" + i)) {
					this.uuidList.add(nbt.getUuid("uuid_" + i));
				}
				nbt.remove("uuid_" + i);
			}
			nbt.remove("uuid_list_size");
		} else {
			NbtList nbtList = nbt.getList("uuid_list", NbtElement.INT_ARRAY_TYPE);
			for (NbtElement nbtElement : nbtList) {
				this.uuidList.add(NbtHelper.toUuid(nbtElement));
			}
		}

		super.readNbt(nbt, registryLookup);
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
