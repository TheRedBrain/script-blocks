package com.github.theredbrain.scriptblocks.client.render.renderer;

import com.github.theredbrain.scriptblocks.client.render.model.BossEntityModel;
import com.github.theredbrain.scriptblocks.entity.mob.BossEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

public class BossEntityRenderer extends GeoEntityRenderer<BossEntity> {
    public BossEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new BossEntityModel());
    }
}
