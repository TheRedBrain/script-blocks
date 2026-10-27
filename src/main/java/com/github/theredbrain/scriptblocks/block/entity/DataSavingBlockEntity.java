package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesData;
import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DataSavingBlockEntity extends BlockEntity implements Resetable, ProvidesData {
	private final HashMap<String, String> data = new HashMap<>(Map.of());

	public DataSavingBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.DATA_SAVING_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput view) {

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("data", CompoundTag.CODEC);
		this.data.forEach((key, entry) -> {
			CompoundTag nbtCompound = new CompoundTag();
			nbtCompound.putString("key", key);
			nbtCompound.putString("value", entry);
			listAppender.add(nbtCompound.copy());
		});

		super.saveAdditional(view);
	}

	@Override
	protected void loadAdditional(ValueInput view) {

		this.data.clear();
		for (CompoundTag nbtCompound : view.listOrEmpty("data", CompoundTag.CODEC)) {
			this.data.put(
					nbtCompound.getStringOr("key", ""),
					nbtCompound.getStringOr("value", "")
			);
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
	public String getData(String id) {
		return this.data.getOrDefault(id, "");
	}

	@Override
	public void setData(String id, String value) {
		this.data.put(id, value);
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}

	// region --- getter & setter ---
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
	// endregion --- getter & setter ---

	@Override
	public void reset() {
		this.data.clear();
		this.setChanged();
		if (this.level != null) {
			BlockState blockState = this.level.getBlockState(this.worldPosition);
			this.level.sendBlockUpdated(this.worldPosition, blockState, blockState, Block.UPDATE_ALL);
		}
	}
}
