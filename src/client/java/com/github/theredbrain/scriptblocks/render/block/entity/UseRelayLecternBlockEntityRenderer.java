package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.UseRelayLecternBlock;
import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.block.entity.UseRelayLecternBlockEntity;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

@Environment(value = EnvType.CLIENT)
public class UseRelayLecternBlockEntityRenderer implements BlockEntityRenderer<UseRelayLecternBlockEntity> {
	private final BookModel book;

	public UseRelayLecternBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
		this.book = new BookModel(ctx.getLayerModelPart(EntityModelLayers.BOOK));
	}

	public void render(UseRelayLecternBlockEntity useRelayLecternBlockEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		BlockState blockState = useRelayLecternBlockEntity.getCachedState();
		if ((Boolean)blockState.get(UseRelayLecternBlock.HAS_BOOK)) {
			matrixStack.push();
			matrixStack.translate(0.5F, 1.0625F, 0.5F);
			float g = ((Direction)blockState.get(UseRelayLecternBlock.FACING)).rotateYClockwise().asRotation();
			matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-g));
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67.5F));
			matrixStack.translate(0.0F, -0.125F, 0.0F);
			this.book.setPageAngles(0.0F, 0.1F, 0.9F, 1.2F);
			VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntitySolid);
			this.book.renderBook(matrixStack, vertexConsumer, i, j, -1);
			matrixStack.pop();
		}
	}
}