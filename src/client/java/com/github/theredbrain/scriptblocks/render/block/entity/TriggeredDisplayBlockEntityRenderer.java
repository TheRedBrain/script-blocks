package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

@Environment(value = EnvType.CLIENT)
public class TriggeredDisplayBlockEntityRenderer implements BlockEntityRenderer<TriggeredDisplayBlockEntity> {
	private final Font textRenderer;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	protected float shadowRadius;
	protected float shadowOpacity = 1.0F;

	public TriggeredDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.textRenderer = ctx.getTextRenderer();
		this.blockEntityRenderDispatcher = ctx.getRenderDispatcher();
	}

	@Override
	public void render(TriggeredDisplayBlockEntity triggeredDisplayBlockEntity, float tickDelta, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int light, int overlay) {

//		this.displayOffset = new Vec3d(triggeredDisplayBlockEntity.getDisplayOffsetX(), triggeredDisplayBlockEntity.getDisplayOffsetY(), triggeredDisplayBlockEntity.getDisplayOffsetZ());
		TriggeredDisplayBlockEntity.RenderState renderState = triggeredDisplayBlockEntity.getRenderState();
		if (triggeredDisplayBlockEntity.getIsTriggered() && renderState != null) {

			TriggeredDisplayBlockEntity.Data object = triggeredDisplayBlockEntity.getData();
			if (object != null) {
				float lerpProgress = triggeredDisplayBlockEntity.getLerpProgress(tickDelta);
				this.shadowRadius = renderState.shadowRadius().lerp(lerpProgress);
				this.shadowOpacity = renderState.shadowStrength().lerp(lerpProgress);
				int j = renderState.brightnessOverride();
				int brightness = j != -1 ? j : light;
				matrixStack.pushPose();

				matrixStack.translate(triggeredDisplayBlockEntity.getDisplayOffset().x, triggeredDisplayBlockEntity.getDisplayOffset().y, triggeredDisplayBlockEntity.getDisplayOffset().z);
				matrixStack.mulPose(this.getBillboardRotation(renderState, triggeredDisplayBlockEntity, tickDelta, new Quaternionf()));
				Transformation affineTransformation = (Transformation) renderState.transformation().interpolate(lerpProgress);
				matrixStack.mulPose(affineTransformation.getMatrix());
				if (triggeredDisplayBlockEntity.getDisplayMode() == TriggeredDisplayBlockEntity.DisplayMode.TEXT) {
					this.renderTextMode(triggeredDisplayBlockEntity, object, matrixStack, vertexConsumerProvider, brightness, lerpProgress);
				}
				matrixStack.popPose();
//				if (triggeredDisplayBlockEntity.getDisplayMode() == TriggeredDisplayBlockEntity.DisplayMode.TEXT) {
//
//					this.renderText(triggeredDisplayBlockEntity, matrixStack, vertexConsumerProvider, light);
//				}
			}
//			WorldRenderer.drawBox(matrixStack, vertexConsumer, m, g, n, o, h, p, 0.9f, 0.9f, 0.9f, 1.0f, 0.5f, 0.5f, 0.5f);
//            WorldRenderer.drawBox(matrixStack, vertexConsumer, x, y, z, x + 1, y + 1, z + 1,  0.0f, 1.0f, 0.0f, 1.0f, 0.5f, 0.5f, 0.5f);
//			this.renderInvisibleBlocks(triggeredDisplayBlockEntity, vertexConsumer, blockPos, matrixStack);
		}
//        if (teleporterBlockBlockEntity.getMode() == StructureBlockMode.SAVE && teleporterBlockBlockEntity.shouldShowAir()) {
//            this.renderInvisibleBlocks(teleporterBlockBlockEntity, vertexConsumer, blockPos, matrixStack);
//        }
	}

//	public void renderItemMode(
//			TriggeredDisplayBlockEntity itemDisplayEntity,
//			TriggeredDisplayBlockEntity.Data data,
//			MatrixStack matrixStack,
//			VertexConsumerProvider vertexConsumerProvider,
//			int i,
//			float f
//	) {
//		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.PI));
//		this.itemRenderer
//				.renderItem(
//						data.itemStack(),
//						data.itemTransform(),
//						i,
//						OverlayTexture.DEFAULT_UV,
//						matrixStack,
//						vertexConsumerProvider,
//						itemDisplayEntity.getWorld(),
//						itemDisplayEntity.getId()
//				);
//	}

	public void renderTextMode(
			TriggeredDisplayBlockEntity triggeredDisplayBlockEntity,
			TriggeredDisplayBlockEntity.Data data,
			PoseStack matrixStack,
			MultiBufferSource vertexConsumerProvider,
			int brightness,
			float lerpProgress
	) {
		byte b = data.flags();
		boolean bl = (b & TriggeredDisplayBlockEntity.SEE_THROUGH_FLAG) != 0;
		boolean bl2 = (b & TriggeredDisplayBlockEntity.DEFAULT_BACKGROUND_FLAG) != 0;
		boolean bl3 = (b & TriggeredDisplayBlockEntity.SHADOW_FLAG) != 0;
		TriggeredDisplayBlockEntity.TextAlignment textAlignment = TriggeredDisplayBlockEntity.getAlignment(b);
		byte c = (byte) data.textOpacity().lerp(lerpProgress);
		int j;
		if (bl2) {
			float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
			j = (int) (g * 255.0F) << 24;
		} else {
			j = data.backgroundColor().lerp(lerpProgress);
		}

		float g = 0.0F;
		Matrix4f matrix4f = matrixStack.last().pose();
		matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
		matrix4f.scale(-0.025F, -0.025F, -0.025F);
		TriggeredDisplayBlockEntity.TextLines textLines = triggeredDisplayBlockEntity.splitLines(this::getLines);
		int k = 9 + 1;
		int l = textLines.width();
		int m = textLines.lines().size() * k;
		matrix4f.translate(1.0F - (float) l / 2.0F, (float) (-m), 0.0F);
		if (j != 0) {
			VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(bl ? RenderType.getTextBackgroundSeeThrough() : RenderType.getTextBackground());
			vertexConsumer.addVertex(matrix4f, -1.0F, -1.0F, 0.0F).setColor(j).setLight(brightness);
			vertexConsumer.addVertex(matrix4f, -1.0F, (float) m, 0.0F).setColor(j).setLight(brightness);
			vertexConsumer.addVertex(matrix4f, (float) l, (float) m, 0.0F).setColor(j).setLight(brightness);
			vertexConsumer.addVertex(matrix4f, (float) l, -1.0F, 0.0F).setColor(j).setLight(brightness);
		}

		for (TriggeredDisplayBlockEntity.TextLine textLine : textLines.lines()) {
			float h = switch (textAlignment) {
				case LEFT -> 0.0F;
				case RIGHT -> (float) (l - textLine.width());
				case CENTER -> (float) l / 2.0F - (float) textLine.width() / 2.0F;
				default -> throw new MatchException(null, null);
			};
			this.textRenderer
					.drawInBatch(
							textLine.contents(),
							h,
							g,
							c << 24 | 16777215,
							bl3,
							matrix4f,
							vertexConsumerProvider,
							bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
							0,
							brightness
					);
			g += (float) k;
		}
	}

	private TriggeredDisplayBlockEntity.TextLines getLines(Component text, int width) {
		List<FormattedCharSequence> list = this.textRenderer.split(text, width);
		List<TriggeredDisplayBlockEntity.TextLine> list2 = new ArrayList<>(list.size());
		int i = 0;

		for (FormattedCharSequence orderedText : list) {
			int j = this.textRenderer.width(orderedText);
			i = Math.max(i, j);
			list2.add(new TriggeredDisplayBlockEntity.TextLine(orderedText, j));
		}

		return new TriggeredDisplayBlockEntity.TextLines(list2, i);
	}

	private Quaternionf getBillboardRotation(TriggeredDisplayBlockEntity.RenderState renderState, TriggeredDisplayBlockEntity entity, float yaw, Quaternionf rotation) {
		Camera camera = this.blockEntityRenderDispatcher.camera;

		return switch (renderState.billboardConstraints()) {
			case FIXED ->
					rotation.rotationYXZ((float) (-Math.PI / 180.0) * lerpYaw(entity, yaw), (float) (Math.PI / 180.0) * lerpPitch(entity, yaw), 0.0F);
			case HORIZONTAL ->
					rotation.rotationYXZ((float) (-Math.PI / 180.0) * lerpYaw(entity, yaw), (float) (Math.PI / 180.0) * getNegatedPitch(camera), 0.0F);
			case VERTICAL ->
					rotation.rotationYXZ((float) (-Math.PI / 180.0) * getBackwardsYaw(camera), (float) (Math.PI / 180.0) * lerpPitch(entity, yaw), 0.0F);
			case CENTER ->
					rotation.rotationYXZ((float) (-Math.PI / 180.0) * getBackwardsYaw(camera), (float) (Math.PI / 180.0) * getNegatedPitch(camera), 0.0F);
			default -> throw new MatchException(null, null);
		};
	}

	private static float getBackwardsYaw(Camera camera) {
		return camera.yRot() - 180.0F;
	}

	private static float getNegatedPitch(Camera camera) {
		return -camera.xRot();
	}

	private static <T extends Display> float lerpYaw(TriggeredDisplayBlockEntity entity, float delta) {
		return Mth.rotLerp(delta, entity.getPrevDisplayYaw(), entity.getDisplayYaw());
	}

	private static <T extends Display> float lerpPitch(TriggeredDisplayBlockEntity entity, float delta) {
		return Mth.lerp(delta, entity.getPrevDisplayPitch(), entity.getDisplayPitch());
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

