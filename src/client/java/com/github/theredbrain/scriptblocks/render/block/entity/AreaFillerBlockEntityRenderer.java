package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

@Environment(value = EnvType.CLIENT)
public class AreaFillerBlockEntityRenderer implements BlockEntityRenderer<AreaFillerBlockEntity> {
	public AreaFillerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(AreaFillerBlockEntity entity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		double o;
		double n;
		double m;
		double k;
		LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;
		if (clientPlayerEntity == null || !clientPlayerEntity.canUseGameMasterBlocks() && !clientPlayerEntity.isSpectator()) {
			return;
		}
		BlockPos blockPos = entity.getAreaPositionOffset();
		Vec3i vec3i = entity.getAreaDimensions();
		if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
			return;
		}
		double d = blockPos.getX();
		double e = blockPos.getZ();
		double g = blockPos.getY();
		double h = g + (double) vec3i.getY();
		k = vec3i.getX(); // temp
		double l = vec3i.getZ(); // temp
		m = k < 0.0 ? d + 1.0 : d; // temp
		n = l < 0.0 ? e + 1.0 : e; // temp
		o = m + k; // temp
		double p = n + l; // temp
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.getLines());
		if (entity.showArea()) {
			LevelRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
		}
	}

	@Override
	public boolean rendersOutsideBoundingBox(AreaFillerBlockEntity areaFillerBlockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

