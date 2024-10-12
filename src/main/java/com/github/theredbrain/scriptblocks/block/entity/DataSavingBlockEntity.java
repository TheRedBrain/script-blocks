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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSavingBlockEntity extends BlockEntity implements Resetable, ProvidesData {
	private HashMap<String, Integer> data = new HashMap<>(Map.of());

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
			nbt.putInt("value_" + i, this.data.get(key));
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int dataSize = nbt.getInt("dataSize");
		this.data.clear();
		for (int i = 0; i < dataSize; i++) {
			if (nbt.contains("key_" + i, NbtElement.STRING_TYPE) && nbt.contains("value_" + i, NbtElement.INT_TYPE)) {
				String key = nbt.getString("key_" + i);
				int value = nbt.getInt("value_" + i);
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
	public int getData(String id) {
		return this.data.getOrDefault(id, 0);
	}

	@Override
	public void setData(String id, int value) {
		this.data.put(id, value);
	}

	@Override
	public void addData(String id, int value) {
		this.setData(id, this.getData(id) + value);
	}

	@Override
	public void reset() {
		this.data.clear();
	}
}
