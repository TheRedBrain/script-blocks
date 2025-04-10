package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.DecoratedPotPatterns;
import net.minecraft.block.entity.DecoratedPotBlockEntity;
import net.minecraft.block.entity.Sherds;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

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

	public AestheticDecoratedPotBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
		ModelPart modelPart = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_BASE);
		this.neck = modelPart.getChild(EntityModelPartNames.NECK);
		this.top = modelPart.getChild("top");
		this.bottom = modelPart.getChild("bottom");
		ModelPart modelPart2 = context.getLayerModelPart(EntityModelLayers.DECORATED_POT_SIDES);
		this.front = modelPart2.getChild("front");
		this.back = modelPart2.getChild("back");
		this.left = modelPart2.getChild("left");
		this.right = modelPart2.getChild("right");
	}

	private static SpriteIdentifier getTextureIdFromSherd(Optional<Item> sherd) {
		if (sherd.isPresent()) {
			SpriteIdentifier spriteIdentifier = TexturedRenderLayers.getDecoratedPotPatternTextureId(DecoratedPotPatterns.fromSherd((Item) sherd.get()));
			if (spriteIdentifier != null) {
				return spriteIdentifier;
			}
		}

		return TexturedRenderLayers.DECORATED_POT_SIDE;
	}

	public void render(
			AestheticDecoratedPotBlockEntity aestheticDecoratedPotBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j
	) {
		matrixStack.push();
		Direction direction = aestheticDecoratedPotBlockEntity.getHorizontalFacing();
		matrixStack.translate(0.5, 0.0, 0.5);
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F - direction.asRotation()));
		matrixStack.translate(-0.5, 0.0, -0.5);
		DecoratedPotBlockEntity.WobbleType wobbleType = aestheticDecoratedPotBlockEntity.lastWobbleType;
		if (wobbleType != null && aestheticDecoratedPotBlockEntity.getWorld() != null) {
			float g = ((float) (aestheticDecoratedPotBlockEntity.getWorld().getTime() - aestheticDecoratedPotBlockEntity.lastWobbleTime) + f) / (float) wobbleType.lengthInTicks;
			if (g >= 0.0F && g <= 1.0F) {
				if (wobbleType == DecoratedPotBlockEntity.WobbleType.POSITIVE) {
					float h = 0.015625F;
					float k = g * (float) (Math.PI * 2);
					float l = -1.5F * (MathHelper.cos(k) + 0.5F) * MathHelper.sin(k / 2.0F);
					matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(l * 0.015625F), 0.5F, 0.0F, 0.5F);
					float m = MathHelper.sin(k);
					matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(m * 0.015625F), 0.5F, 0.0F, 0.5F);
				} else {
					float h = MathHelper.sin(-g * 3.0F * (float) Math.PI) * 0.125F;
					float k = 1.0F - g;
					matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(h * k), 0.5F, 0.0F, 0.5F);
				}
			}
		}

		VertexConsumer vertexConsumer = TexturedRenderLayers.DECORATED_POT_BASE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
		this.neck.render(matrixStack, vertexConsumer, i, j);
		this.top.render(matrixStack, vertexConsumer, i, j);
		this.bottom.render(matrixStack, vertexConsumer, i, j);
		Sherds sherds = aestheticDecoratedPotBlockEntity.getSherds();
		this.renderDecoratedSide(this.front, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.front()));
		this.renderDecoratedSide(this.back, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.back()));
		this.renderDecoratedSide(this.left, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.left()));
		this.renderDecoratedSide(this.right, matrixStack, vertexConsumerProvider, i, j, getTextureIdFromSherd(sherds.right()));
		matrixStack.pop();
	}

	private void renderDecoratedSide(
			ModelPart part, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, SpriteIdentifier textureId
	) {
		part.render(matrices, textureId.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid), light, overlay);
	}
}
