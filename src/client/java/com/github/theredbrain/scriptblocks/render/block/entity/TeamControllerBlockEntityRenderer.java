package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.TeamControllerBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value = EnvType.CLIENT)
public class TeamControllerBlockEntityRenderer implements BlockEntityRenderer<TeamControllerBlockEntity> {
	public TeamControllerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(TeamControllerBlockEntity blockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		double o;
		double n;
		double m;
		double k;
		LocalPlayer clientPlayerEntity = Minecraft.getInstance().player;
		if (clientPlayerEntity == null || !clientPlayerEntity.canUseGameMasterBlocks() && !clientPlayerEntity.isSpectator()) {
			return;
		}
		BlockPos blockPos = blockEntity.getAreaPositionOffset();
		Vec3i vec3i = blockEntity.getAreaDimensions();
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
		if (blockEntity.showArea()) {
			LevelRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
			this.renderInvisibleBlocks(blockEntity, vertexConsumer, blockPos, matrixStack);
		}
	}

	private void renderInvisibleBlocks(TeamControllerBlockEntity entity, VertexConsumer vertices, BlockPos pos, PoseStack matrices) {
		Level blockView = entity.getWorld();
		BlockPos blockPos = entity.getPos();
		BlockPos blockPos2 = blockPos.offset(pos);
		if (blockView != null) {
			for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos2, blockPos2.offset(entity.getAreaDimensions()).add(-1, -1, -1))) {
				boolean bl5;
				BlockState blockState = blockView.getBlockState(blockPos3);
				boolean bl = blockState.isAir();
				boolean bl2 = blockState.is(Blocks.STRUCTURE_VOID);
				boolean bl3 = blockState.is(Blocks.BARRIER);
				boolean bl4 = blockState.is(Blocks.LIGHT);
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
					LevelRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 0.5f, 0.5f, 1.0f, 1.0f, 0.5f, 0.5f, 1.0f);
					continue;
				}
				if (bl2) {
					LevelRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.75f, 0.75f, 1.0f, 1.0f, 0.75f, 0.75f);
					continue;
				}
				if (bl3) {
					LevelRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
					continue;
				}
				if (!bl4) continue;
				LevelRenderer.drawBox(matrices, vertices, d, e, g, h, i, j, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f);
			}
		}
	}

	@Override
	public boolean rendersOutsideBoundingBox(TeamControllerBlockEntity teamControllerBlockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

