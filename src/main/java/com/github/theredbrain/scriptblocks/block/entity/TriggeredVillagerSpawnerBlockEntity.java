package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;

public class TriggeredVillagerSpawnerBlockEntity extends TriggeredSpawnerBlockEntity {

	private static final VillagerData DEFAULT_VILLAGER_DATA = new VillagerData(Registries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS), Registries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE), 1);
	private VillagerData villagerData = DEFAULT_VILLAGER_DATA;

	public TriggeredVillagerSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	protected void writeData(WriteView view) {

		view.put("villager_data", VillagerData.CODEC, this.getVillagerData());

		super.writeData(view);

	}

	protected void readData(ReadView view) {

		this.villagerData = view.read("villager_data", VillagerData.CODEC).orElse(DEFAULT_VILLAGER_DATA);

		super.readData(view);

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
