package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.Resetable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractiveLootBlockEntity extends BlockEntity implements Resetable {
	private Set<UUID> playerSet = new HashSet<>();
	private String lootTableIdentifierString = "";

	public InteractiveLootBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.INTERACTIVE_LOOT_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		List<UUID> list = this.playerSet.stream().toList();
		int listSize = list.size();
		nbt.putInt("listSize", listSize);
		for (int i = 0; i < listSize; i++) {
			nbt.putUuid("listEntry_" + i, list.get(i));
		}

		if (!this.lootTableIdentifierString.equals("")) {
			nbt.putString("lootTableIdentifierString", this.lootTableIdentifierString);
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.playerSet.clear();
		int listSize = nbt.getInt("listSize");
		for (int i = 0; i < listSize; i++) {
			if (nbt.containsUuid("listEntry_" + i)) {
				this.playerSet.add(nbt.getUuid("listEntry_" + i));
			}
		}

		this.lootTableIdentifierString = nbt.getString("lootTableIdentifierString");

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	public String getLootTableIdentifierString() {
		return this.lootTableIdentifierString;
	}

	public void setLootTableIdentifierString(String lootTableIdentifierString) {
		this.lootTableIdentifierString = lootTableIdentifierString;
	}

	public boolean isPlayerInSet(PlayerEntity playerEntity) {
		return this.playerSet.contains(playerEntity.getUuid());
	}

	public boolean addPlayerToSet(PlayerEntity playerEntity) {
		return this.playerSet.add(playerEntity.getUuid());
	}

	@Override
	public void reset() {
		this.playerSet.clear();
	}
}
