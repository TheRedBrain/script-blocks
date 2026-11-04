package com.github.theredbrain.scriptblocks.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.world.entity.npc.villager.VillagerData;
import org.jspecify.annotations.Nullable;

public class FakeVillagerRenderState extends HoldingEntityRenderState implements VillagerDataHolderRenderState {
	public boolean isUnhappy;
	public @Nullable VillagerData villagerData;

	@Override
	public @Nullable VillagerData getVillagerData() {
		return this.villagerData;
	}
}
