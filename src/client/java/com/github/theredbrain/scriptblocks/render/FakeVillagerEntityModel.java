package com.github.theredbrain.scriptblocks.render;

import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class FakeVillagerEntityModel<T extends Entity> extends VillagerResemblingModel<T> {

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
			this.getHead().roll = 0.3F * MathHelper.sin(0.45F * animationProgress);
			this.getHead().pitch = 0.4F;
		} else {
			this.getHead().roll = 0.0F;
		}
	}
}
