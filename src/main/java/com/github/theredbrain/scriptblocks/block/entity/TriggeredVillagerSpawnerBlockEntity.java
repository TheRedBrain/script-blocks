package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class TriggeredVillagerSpawnerBlockEntity extends TriggeredSpawnerBlockEntity {

	private static final VillagerData DEFAULT_VILLAGER_DATA = new VillagerData(BuiltInRegistries.VILLAGER_TYPE.getOrThrow(VillagerType.PLAINS), BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(VillagerProfession.NONE), 1);
	private VillagerData villagerData = DEFAULT_VILLAGER_DATA;

	public TriggeredVillagerSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.TRIGGERED_VILLAGER_SPAWNER_BLOCK_ENTITY, pos, state);
	}

	protected void saveAdditional(ValueOutput view) {

		view.store("villager_data", VillagerData.CODEC, this.getVillagerData());

		super.saveAdditional(view);

	}

	protected void loadAdditional(ValueInput view) {

		this.villagerData = view.read("villager_data", VillagerData.CODEC).orElse(DEFAULT_VILLAGER_DATA);

		super.loadAdditional(view);

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
		if (entity instanceof VillagerDataHolder villagerDataContainer) {
			villagerDataContainer.setVillagerData(this.villagerData);
		}
	}
}
