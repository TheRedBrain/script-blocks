package com.github.theredbrain.scriptblocks.renderer.blockentity;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayLecternBlockEntity;
import com.github.theredbrain.scriptblocks.renderer.blockentity.state.UseRelayLecternRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value = EnvType.CLIENT)
public class UseRelayLecternRenderer implements BlockEntityRenderer<UseRelayLecternBlockEntity, UseRelayLecternRenderState> {
	private final SpriteGetter sprites;
	private final BookModel bookModel;
	private static final BookModel.State BOOK_STATE = BookModel.State.forAnimation(0.0F, 0.1F, 0.9F, 1.2F);

	public UseRelayLecternRenderer(final BlockEntityRendererProvider.Context context) {
		this.sprites = context.sprites();
		this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
	}

	@Override
	public UseRelayLecternRenderState createRenderState() {
		return new UseRelayLecternRenderState();
	}

	@Override
	public void extractRenderState(
			final UseRelayLecternBlockEntity blockEntity,
			final UseRelayLecternRenderState state,
			final float partialTicks,
			final Vec3 cameraPosition,
			final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.hasBook = blockEntity.getBlockState().getValue(BlockStateProperties.HAS_BOOK);
		state.yRot = blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).getClockWise().toYRot();
	}

	@Override
	public void submit(final UseRelayLecternRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
		if (state.hasBook) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 1.0625F, 0.5F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.yRot));
			poseStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
			poseStack.translate(0.0F, -0.125F, 0.0F);
			submitNodeCollector.submitModel(
					this.bookModel,
					BOOK_STATE,
					poseStack,
					state.lightCoords,
					OverlayTexture.NO_OVERLAY,
					-1,
					EnchantTableRenderer.BOOK_TEXTURE,
					this.sprites,
					0,
					state.breakProgress
			);
			poseStack.popPose();
		}
	}
}
