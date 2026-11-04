package com.github.theredbrain.scriptblocks.renderer.entity;

import com.github.theredbrain.scriptblocks.entity.passive.FakeVillagerEntity;
import com.github.theredbrain.scriptblocks.model.npc.FakeBabyVillagerModel;
import com.github.theredbrain.scriptblocks.model.npc.FakeVillagerModel;
import com.github.theredbrain.scriptblocks.renderer.entity.state.FakeVillagerRenderState;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.resources.Identifier;

public class FakeVillagerEntityRenderer extends AgeableMobRenderer<FakeVillagerEntity, FakeVillagerRenderState, FakeVillagerModel> {
	private static final Identifier VILLAGER_BASE_LOCATION = Identifier.withDefaultNamespace("textures/entity/villager/villager.png");
	private static final Identifier VILLAGER_BABY_LOCATION = Identifier.withDefaultNamespace("textures/entity/villager/villager_baby.png");
	public static final CustomHeadLayer.Transforms CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(-0.1171875F, -0.07421875F, 1.0F);

	public FakeVillagerEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new FakeVillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), new FakeBabyVillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY)), 0.5F);
		this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache(), CUSTOM_HEAD_TRANSFORMS));
		this.addLayer(
				new VillagerProfessionLayer<>(
						this,
						context.getResourceManager(),
						"villager",
						new FakeVillagerModel(context.bakeLayer(ModelLayers.VILLAGER_NO_HAT)),
						new FakeBabyVillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY_NO_HAT))
				)
		);
		this.addLayer(new CrossedArmsItemLayer<>(this));
	}

	@Override
	public FakeVillagerRenderState createRenderState() {
		return new FakeVillagerRenderState();
	}

	@Override
	public void extractRenderState(final FakeVillagerEntity entity, final FakeVillagerRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		HoldingEntityRenderState.extractHoldingEntityRenderState(entity, state, this.itemModelResolver);
//		state.isUnhappy = entity.getUnhappyCounter() > 0; // TODO is this getHeadRollingTimeLeft() ?
		state.villagerData = entity.getVillagerData();
	}

	@Override
	public Identifier getTextureLocation(final FakeVillagerRenderState state) {
		return state.isBaby ? VILLAGER_BABY_LOCATION : VILLAGER_BASE_LOCATION;
	}

	@Override
	protected float getShadowRadius(final FakeVillagerRenderState state) {
		float radius = super.getShadowRadius(state);
		return state.isBaby ? radius * 0.5F : radius;
	}
}
