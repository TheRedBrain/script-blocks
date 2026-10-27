package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.UseRelayLecternBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Environment(value = EnvType.CLIENT)
public class UseRelayLecternBlockEntityRenderer implements BlockEntityRenderer<UseRelayLecternBlockEntity> {
	private final BookModel book;

	public UseRelayLecternBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.book = new BookModel(ctx.bakeLayer(ModelLayers.BOOK));
	}

	public void render(UseRelayLecternBlockEntity useRelayLecternBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		BlockState blockState = useRelayLecternBlockEntity.getCachedState();
		if (blockState.getValue(BlockStateProperties.HAS_BOOK)) {
			matrixStack.pushPose();
			matrixStack.translate(0.5F, 1.0625F, 0.5F);
			float g = blockState.getValue(HorizontalDirectionalBlock.FACING).getClockWise().asRotation();
			matrixStack.mulPose(Axis.YP.rotationDegrees(-g));
			matrixStack.mulPose(Axis.ZP.rotationDegrees(67.5F));
			matrixStack.translate(0.0F, -0.125F, 0.0F);
			this.book.setPageAngles(0.0F, 0.1F, 0.9F, 1.2F);
			VertexConsumer vertexConsumer = EnchantTableRenderer.BOOK_TEXTURE.buffer(vertexConsumerProvider, RenderType::getEntitySolid);
			this.book.renderBook(matrixStack, vertexConsumer, i, j, -1);
			matrixStack.popPose();
		}
	}
}