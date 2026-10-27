package com.github.theredbrain.scriptblocks.render;

import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class FakeVillagerEntityModel<T extends Entity> extends VillagerModel<T> {

	public FakeVillagerEntityModel(ModelPart modelPart) {
		super(modelPart);
	}

	public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
		super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
		boolean bl = false;
		if (entity instanceof FakeVillagerEntity) {
			bl = ((FakeVillagerEntity) entity).getHeadRollingTimeLeft() > 0;
		}
		if (bl) {
			this.getHead().roll = 0.3F * Mth.sin(0.45F * animationProgress);
			this.getHead().pitch = 0.4F;
		} else {
			this.getHead().roll = 0.0F;
		}
	}
}
