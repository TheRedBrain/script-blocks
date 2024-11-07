package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSavingBlockEntity extends BlockEntity implements Resetable, ProvidesData {
	private final HashMap<String, String> data = new HashMap<>(Map.of());

	public DataSavingBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.DATA_SAVING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		List<String> keyList = this.data.keySet().stream().toList();
		int dataSize = this.data.keySet().size();
		nbt.putInt("dataSize", dataSize);
		for (int i = 0; i < dataSize; i++) {
			String key = keyList.get(i);
			nbt.putString("key_" + i, key);
			nbt.putString("value_" + i, this.data.get(key));
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int dataSize = nbt.getInt("dataSize");
		this.data.clear();
		for (int i = 0; i < dataSize; i++) {
			if (nbt.contains("key_" + i, NbtElement.STRING_TYPE) && nbt.contains("value_" + i, NbtElement.STRING_TYPE)) {
				String key = nbt.getString("key_" + i);
				String value = nbt.getString("value_" + i);
				this.data.put(key, value);
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
	public String getData(String id) {
		return this.data.getOrDefault(id, "");
	}

	@Override
	public void setData(String id, String value) {
		this.data.put(id, value);
	}

	public List<MutablePair<String, String>> getDataList() {
		List<MutablePair<String, String>> dataList = new ArrayList<>();
		for (Map.Entry<String, String> entry : this.data.entrySet()) {
			dataList.add(new MutablePair<>(entry.getKey(), entry.getValue()));
		}
		return dataList;
	}

	public void setDataList(List<MutablePair<String, String>> dataList) {
		this.data.clear();
		for (MutablePair<String, String> listEntry : dataList) {
			this.data.put(listEntry.left, listEntry.right);
		}
	}
	@Override
	public void reset() {
		this.data.clear();
	}
}
