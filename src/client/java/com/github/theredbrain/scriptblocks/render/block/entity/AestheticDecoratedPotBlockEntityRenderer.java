package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import java.util.Optional;


@Environment(EnvType.CLIENT)
public class AestheticDecoratedPotBlockEntityRenderer implements BlockEntityRenderer<AestheticDecoratedPotBlockEntity> {
	private final ModelPart neck;
	private final ModelPart front;
	private final ModelPart back;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart top;
	private final ModelPart bottom;

	public AestheticDecoratedPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		ModelPart modelPart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
		this.neck = modelPart.getChild(PartNames.NECK);
		this.top = modelPart.getChild("top");
		this.bottom = modelPart.getChild("bottom");
		ModelPart modelPart2 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
		this.front = modelPart2.getChild("front");
		this.back = modelPart2.getChild("back");
		this.left = modelPart2.getChild("left");
		this.right = modelPart2.getChild("right");
	}

	private static Material getTextureIdFromSherd(Optional<Item> sherd) {
		if (sherd.isPresent()) {
			Material spriteIdentifier = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem((Item) sherd.get()));
			if (spriteIdentifier != null) {
				return spriteIdentifier;
			}
		}

		return Sheets.DECORATED_POT_SIDE;
	}

	public void render(
			AestheticDecoratedPotBlockEntity aestheticDecoratedPotBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j
	) {
		matrixStack.pushPose();
		Direction direction = aestheticDecoratedPotBlockEntity.getHorizontalFacing();
		matrixStack.translate(0.5, 0.0, 0.5);
		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - direction.asRotation()));
		matrixStack.translate(-0.5, 0.0, -0.5);
		DecoratedPotBlockEntity.WobbleStyle wobbleType = aestheticDecoratedPotBlockEntity.lastWobbleType;
		if (wobbleType != null && aestheticDecoratedPotBlockEntity.getWorld() != null) {
			float g = ((float) (aestheticDecoratedPotBlockEntity.getWorld().getTime() - aestheticDecoratedPotBlockEntity.lastWobbleTime) + f) / (float) wobbleType.duration;
			if (g >= 0.0F && g <= 1.0F) {
				if (wobbleType == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
					float h = 0.015625F;
					float k = g * (float) (Math.PI * 2);
					float l = -1.5F * (Mth.cos(k) + 0.5F) * Mth.sin(k / 2.0F);
					matrixStack.rotateAround(Axis.XP.rotation(l * 0.015625F), 0.5F, 0.0F, 0.5F);
					float m = Mth.sin(k);
					matrixStack.rotateAround(Axis.ZP.rotation(m * 0.015625F), 0.5F, 0.0F, 0.5F);
				} else {
					float h = Mth.sin(-g * 3.0F * (float) Math.PI) * 0.125F;
					float k = 1.0F - g;
					matrixStack.rotateAround(Axis.YP.rotation(h * k), 0.5F, 0.0F, 0.5F);
				}
			}
		}

		VertexConsumer vertexConsumer = Sheets.DECORATED_POT_BASE.buffer(vertexConsumerProvider, RenderType::getEntitySolid);
		this.neck.render(matrixStack, vertexConsumer, i, j);
		this.top.render(matrixStack, vertexConsumer, i, j);
		this.bottom.render(matrixStack, vertexConsumer, i, j);
		PotDecorations sherds = aestheticDecoratedPotBlockEntity.getSherds();
		this.renderDecoratedSide(this.front, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.front()));
		this.renderDecoratedSide(this.back, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.back()));
		this.renderDecoratedSide(this.left, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.left()));
		this.renderDecoratedSide(this.right, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.right()));
		matrixStack.popPose();
	}

	private void renderDecoratedSide(
			ModelPart part, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, Material textureId
	) {
		part.render(matrices, textureId.buffer(vertexConsumers, RenderType::getEntitySolid), light, overlay);
	}
}
