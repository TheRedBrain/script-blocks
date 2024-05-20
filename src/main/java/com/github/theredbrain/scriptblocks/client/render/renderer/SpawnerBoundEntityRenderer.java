package com.github.theredbrain.scriptblocks.client.render.renderer;

import com.github.theredbrain.scriptblocks.client.render.model.SpawnerBoundEntityModel;
import com.github.theredbrain.scriptblocks.entity.mob.SpawnerBoundEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class SpawnerBoundEntityRenderer extends GeoEntityRenderer<SpawnerBoundEntity> {
    public SpawnerBoundEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new SpawnerBoundEntityModel());
    }
}
