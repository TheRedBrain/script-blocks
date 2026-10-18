package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.TwoAreasPlayerDetectorBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.world.World;

@Environment(value = EnvType.CLIENT)
public class TwoAreasPlayerDetectorBlockEntityRenderer implements BlockEntityRenderer<TwoAreasPlayerDetectorBlockEntity> {
	public TwoAreasPlayerDetectorBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
	}

	@Override
	public void render(TwoAreasPlayerDetectorBlockEntity entity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().player;
		if (clientPlayerEntity == null || !clientPlayerEntity.isCreativeLevelTwoOp() && !clientPlayerEntity.isSpectator()) {
			return;
		}
		BlockPos firstAreaPositionOffset = entity.getFirstAreaPositionOffset();
		Vec3i firstAreaDimensions = entity.getFirstAreaDimensions();
		BlockPos secondAreaPositionOffset = entity.getSecondAreaPositionOffset();
		Vec3i secondAreaDimensions = entity.getSecondAreaDimensions();
		if (firstAreaDimensions.getX() < 1 || firstAreaDimensions.getY() < 1 || firstAreaDimensions.getZ() < 1 || secondAreaDimensions.getX() < 1 || secondAreaDimensions.getY() < 1 || secondAreaDimensions.getZ() < 1) {
			return;
		}

		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLines());

		double d = firstAreaPositionOffset.getX();
		double e = firstAreaPositionOffset.getZ();
		double g = firstAreaPositionOffset.getY();
		double h = g + (double) firstAreaDimensions.getY();
		double k = firstAreaDimensions.getX();
		double l = firstAreaDimensions.getZ();
		double m = k < 0.0 ? d + 1.0 : d;
		double n = l < 0.0 ? e + 1.0 : e;
		double o = m + k;
		double p = n + l;
		if (entity.showFirstArea()) {
			WorldRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
			this.renderInvisibleBlocks(entity, vertexConsumer, firstAreaPositionOffset, firstAreaDimensions, matrixStack);
		}

		d = secondAreaPositionOffset.getX();
		e = secondAreaPositionOffset.getZ();
		g = secondAreaPositionOffset.getY();
		h = g + (double) secondAreaDimensions.getY();
		k = secondAreaDimensions.getX();
		l = secondAreaDimensions.getZ();
		m = k < 0.0 ? d + 1.0 : d;
		n = l < 0.0 ? e + 1.0 : e;
		o = m + k;
		p = n + l;
		if (entity.showSecondArea()) {
			WorldRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
			this.renderInvisibleBlocks(entity, vertexConsumer, secondAreaPositionOffset, secondAreaDimensions, matrixStack);
		}
	}

	private void renderInvisibleBlocks(TwoAreasPlayerDetectorBlockEntity entity, VertexConsumer vertices, BlockPos pos, Vec3i dimensions, MatrixStack matrices) {
		World blockView = entity.getWorld();
		BlockPos blockPos = entity.getPos();
		BlockPos blockPos2 = blockPos.add(pos);
		if (blockView != null) {
			for (BlockPos blockPos3 : BlockPos.iterate(blockPos2, blockPos2.add(dimensions).add(-1, -1, -1))) {
				boolean bl5;
				BlockState blockState = blockView.getBlockState(blockPos3);
				boolean bl = blockState.isAir();
				boolean bl2 = blockState.isOf(Blocks.STRUCTURE_VOID);
				boolean bl3 = blockState.isOf(Blocks.BARRIER);
				boolean bl4 = blockState.isOf(Blocks.LIGHT);
				boolean bl6 = bl5 = bl2 || bl3 || bl4;
				if (!bl && !bl5) continue;
				float f = bl ? 0.05f : 0.0f;
				double d = (float) (blockPos3.getX() - blockPos.getX()) + 0.45f - f;
				double e = (float) (blockPos3.getY() - blockPos.getY()) + 0.45f - f;
				double g = (float) (blockPos3.getZ() - blockPos.getZ()) + 0.45f - f;
				double h = (float) (blockPos3.getX() - blockPos.getX()) + 0.55f + f;
				double i = (float) (blockPos3.getY() - blockPos.getY()) + 0.55f + f;
				double j = (float) (blockPos3.getZ() - blockPos.getZ()) + 0.55f + f;
				if (bl) {
					WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
					continue;
				}
				if (bl2) {
					WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
					continue;
				}
				if (bl3) {
					WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
					continue;
				}
				if (!bl4) continue;
				WorldRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
			}
		}
	}

	@Override
	public boolean rendersOutsideBoundingBox(TwoAreasPlayerDetectorBlockEntity twoAreasPlayerDetectorBlockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

