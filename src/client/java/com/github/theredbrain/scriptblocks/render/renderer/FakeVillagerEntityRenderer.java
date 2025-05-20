package com.github.theredbrain.scriptblocks.render.renderer;

import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import com.github.theredbrain.scriptblocks.render.FakeVillagerEntityModel;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class FakeVillagerEntityRenderer extends MobEntityRenderer<FakeVillagerEntity, FakeVillagerEntityModel<FakeVillagerEntity>> {
	private static final Identifier TEXTURE = Identifier.of("textures/entity/villager/villager.png");

	public FakeVillagerEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new FakeVillagerEntityModel<>(context.getPart(EntityModelLayers.VILLAGER)), 0.5F);
		this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
		this.addFeature(new VillagerClothingFeatureRenderer<>(this, context.getResourceManager(), "villager"));
		this.addFeature(new VillagerHeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
	}

	public Identifier getTexture(FakeVillagerEntity fakeVillagerEntity) {
		return TEXTURE;
	}

	protected void scale(FakeVillagerEntity fakeVillagerEntity, MatrixStack matrixStack, float f) {
		float g = 0.9375F;
		if (fakeVillagerEntity.isBaby()) {
			g *= 0.5F;
			this.shadowRadius = 0.25F;
		} else {
			this.shadowRadius = 0.5F;
		}

		matrixStack.scale(g, g, g);
	}
}
