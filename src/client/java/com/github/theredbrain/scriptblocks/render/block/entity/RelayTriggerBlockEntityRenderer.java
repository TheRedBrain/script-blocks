package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.RelayTriggerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
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

// TODO clean up
@Environment(value = EnvType.CLIENT)
public class RelayTriggerBlockEntityRenderer
		implements BlockEntityRenderer<RelayTriggerBlockEntity> {
	public RelayTriggerBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(RelayTriggerBlockEntity relayTriggerBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		double o;
		double n;
		double m;
		double k;
		if (!Minecraft.getInstance().player.canUseGameMasterBlocks() && !Minecraft.getInstance().player.isSpectator()) {
			return;
		}
//        BlockPos incomingTeleportPositionOffset = teleporterBlockBlockEntity.getIncomingTeleportPositionOffset();
		BlockPos blockPos = relayTriggerBlockEntity.getAreaPositionOffset();
		Vec3i vec3i = relayTriggerBlockEntity.getAreaDimensions();
		if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
			return;
		}
//        if (teleporterBlockBlockEntity.getMode() != StructureBlockMode.SAVE && teleporterBlockBlockEntity.getMode() != StructureBlockMode.LOAD) {
//            return;
//        }
//        double x = incomingTeleportPositionOffset.getX();
//        double y = incomingTeleportPositionOffset.getY();
//        double z = incomingTeleportPositionOffset.getZ();

		double d = blockPos.getX();
		double e = blockPos.getZ();
		double g = blockPos.getY();
		double h = g + (double) vec3i.getY();
//        double l = switch (teleporterBlockBlockEntity.getMirror()) {
//            case BlockMirror.LEFT_RIGHT -> {
//                k = vec3i.getX();
//                yield -vec3i.getZ();
//            }
//            case BlockMirror.FRONT_BACK -> {
//                k = -vec3i.getX();
//                yield vec3i.getZ();
//            }
//            default -> {
//                k = vec3i.getX();
//                yield vec3i.getZ();
//            }
//        };
		k = vec3i.getX(); // temp
		double l = vec3i.getZ(); // temp
//        double p = switch (teleporterBlockBlockEntity.getRotation()) {
//            case BlockRotation.CLOCKWISE_90 -> {
//                m = l < 0.0 ? d : d + 1.0;
//                n = k < 0.0 ? e + 1.0 : e;
//                o = m - l;
//                yield n + k;
//            }
//            case BlockRotation.CLOCKWISE_180 -> {
//                m = k < 0.0 ? d : d + 1.0;
//                n = l < 0.0 ? e : e + 1.0;
//                o = m - k;
//                yield n - l;
//            }
//            case BlockRotation.COUNTERCLOCKWISE_90 -> {
//                m = l < 0.0 ? d + 1.0 : d;
//                n = k < 0.0 ? e : e + 1.0;
//                o = m + l;
//                yield n - k;
//            }
//            default -> {
//                m = k < 0.0 ? d + 1.0 : d;
//                n = l < 0.0 ? e + 1.0 : e;
//                o = m + k;
//                yield n + l;
//            }
//        };
		m = k < 0.0 ? d + 1.0 : d; // temp
		n = l < 0.0 ? e + 1.0 : e; // temp
		o = m + k; // temp
		double p = n + l; // temp
		float q = 1.0f;
		float r = 0.9f;
		float s = 0.5f;
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.getLines());
		if (relayTriggerBlockEntity.getShowArea()) {
			LevelRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
//            WorldRenderer.drawBox(matrixStack, vertexConsumer, x, y, z, x + 1, y + 1, z + 1,  0.0f, 1.0f, 0.0f, 1.0f, 0.5f, 0.5f, 0.5f);
			this.renderInvisibleBlocks(relayTriggerBlockEntity, vertexConsumer, blockPos, matrixStack);
		}
//        if (teleporterBlockBlockEntity.getMode() == StructureBlockMode.SAVE && teleporterBlockBlockEntity.shouldShowAir()) {
//            this.renderInvisibleBlocks(teleporterBlockBlockEntity, vertexConsumer, blockPos, matrixStack);
//        }
	}

	private void renderInvisibleBlocks(RelayTriggerBlockEntity entity, VertexConsumer vertices, BlockPos pos, PoseStack matrices) {
		Level blockView = entity.getWorld();
		BlockPos blockPos = entity.getPos();
		BlockPos blockPos2 = blockPos.offset(pos);
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

	@Override
	public boolean rendersOutsideBoundingBox(RelayTriggerBlockEntity relayTriggerBlockEntity) {
		return true;
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

