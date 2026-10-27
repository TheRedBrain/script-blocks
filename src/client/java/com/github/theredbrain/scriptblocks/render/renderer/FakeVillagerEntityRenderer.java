package com.github.theredbrain.scriptblocks.render.renderer;

import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import com.github.theredbrain.scriptblocks.render.FakeVillagerEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.resources.Identifier;

public class FakeVillagerEntityRenderer extends MobRenderer<FakeVillagerEntity, FakeVillagerEntityModel<FakeVillagerEntity>> {
	private static final Identifier TEXTURE = Identifier.parse("textures/entity/villager/villager.png");

	public FakeVillagerEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new FakeVillagerEntityModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
		this.addFeature(new CustomHeadLayer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
		this.addFeature(new VillagerProfessionLayer<>(this, context.getResourceManager(), "villager"));
		this.addFeature(new CrossedArmsItemLayer<>(this, context.getHeldItemRenderer()));
	}

	public Identifier getTexture(FakeVillagerEntity fakeVillagerEntity) {
		return TEXTURE;
	}

	protected void scale(FakeVillagerEntity fakeVillagerEntity, PoseStack matrixStack, float f) {
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
