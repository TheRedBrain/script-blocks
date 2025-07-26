package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.AreaFillerBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@Environment(value = EnvType.CLIENT)
public class AreaFillerBlockEntityRenderer implements BlockEntityRenderer<AreaFillerBlockEntity> {
	public AreaFillerBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(AreaFillerBlockEntity entity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		double o;
		double n;
		double m;
		double k;
		ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
		if (clientPlayerEntity == null || !clientPlayerEntity.isCreativeLevelTwoOp() && !clientPlayerEntity.isSpectator()) {
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
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());
		if (entity.showArea()) {
			WorldRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
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

