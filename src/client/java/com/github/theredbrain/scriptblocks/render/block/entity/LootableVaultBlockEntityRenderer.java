package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.LootableVaultBlockEntity;
import com.github.theredbrain.scriptblocks.block.lootable_vault.LootableVaultClientData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Deprecated
@Environment(EnvType.CLIENT)
public class LootableVaultBlockEntityRenderer implements BlockEntityRenderer<LootableVaultBlockEntity> {
	private final ItemRenderer itemRenderer;
	private final RandomSource random = RandomSource.create();

	public LootableVaultBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	public void render(LootableVaultBlockEntity lootableVaultBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		if (LootableVaultBlockEntity.Client.hasDisplayItem(lootableVaultBlockEntity.getSharedData())) {
			Level world = lootableVaultBlockEntity.getWorld();
			if (world != null) {
				ItemStack itemStack = lootableVaultBlockEntity.getSharedData().getDisplayItem();
				if (!itemStack.isEmpty()) {
					this.random.setSeed((long) ItemEntityRenderer.getSeed(itemStack));
					LootableVaultClientData clientData = lootableVaultBlockEntity.getClientData();
					renderDisplayItem(
							f,
							world,
							matrixStack,
							vertexConsumerProvider,
							i,
							itemStack,
							this.itemRenderer,
							clientData.getPreviousDisplayRotation(),
							clientData.getDisplayRotation(),
							this.random
					);
				}
			}
		}
	}

	public static void renderDisplayItem(
			float tickDelta,
			Level world,
			PoseStack matrices,
			MultiBufferSource vertexConsumers,
			int light,
			ItemStack stack,
			ItemRenderer itemRenderer,
			float prevRotation,
			float rotation,
			RandomSource random
	) {
		matrices.pushPose();
		matrices.translate(0.5F, 0.4F, 0.5F);
		matrices.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(tickDelta, prevRotation, rotation)));
		ItemEntityRenderer.renderMultipleFromCount(itemRenderer, matrices, vertexConsumers, light, stack, random, world);
		matrices.popPose();
	}
}
