package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class TriggeredVillagerSpawnerBlockEntity extends TriggeredSpawnerBlockEntity {

	private static final VillagerData DEFAULT_VILLAGER_DATA = new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1);
	private VillagerData villagerData = DEFAULT_VILLAGER_DATA;

	public TriggeredVillagerSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		VillagerData.CODEC
				.encodeStart(NbtOps.INSTANCE, this.villagerData)
				.resultOrPartial(ScriptBlocks.LOGGER::error)
				.ifPresent(nbtElement -> nbt.put("villager_data", nbtElement));

		super.writeNbt(nbt, registryLookup);

	}

	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (nbt.contains("VillagerData", NbtElement.COMPOUND_TYPE)) {
			VillagerData.CODEC
					.parse(NbtOps.INSTANCE, nbt.get("VillagerData"))
					.resultOrPartial(ScriptBlocks.LOGGER::error)
					.ifPresent(villagerData -> this.villagerData = villagerData);
		} else {
			VillagerData.CODEC
					.parse(NbtOps.INSTANCE, nbt.get("villager_data"))
					.resultOrPartial(ScriptBlocks.LOGGER::error)
					.ifPresent(villagerData -> this.villagerData = villagerData);
		}

		super.readNbt(nbt, registryLookup);

	}

	// region --- getter & setter ---
	public VillagerData getVillagerData() {
		return this.villagerData;
	}

	public void setVillagerData(VillagerData villagerData) {
		this.villagerData = villagerData;
	}
	//endregion --- getter & setter ---

	@Override
	protected void customInit(Entity entity) {
		if (entity instanceof VillagerDataContainer villagerDataContainer) {
			villagerDataContainer.setVillagerData(this.villagerData);
		}
	}
}
