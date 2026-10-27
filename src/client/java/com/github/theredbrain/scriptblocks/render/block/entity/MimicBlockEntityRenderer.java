package com.github.theredbrain.scriptblocks.render.block.entity;

import com.github.theredbrain.scriptblocks.block.entity.MimicBlockEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksBlocks;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksConfigs;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.RandomSource;

@Environment(value = EnvType.CLIENT)
public class MimicBlockEntityRenderer
		implements BlockEntityRenderer<MimicBlockEntity> {
	public MimicBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
	}

	@Override
	public void render(MimicBlockEntity mimicBlockEntity, float f, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, int j) {
		BlockRenderDispatcher blockRenderManager = Minecraft.getInstance().getBlockRenderer();
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderTypes.getBlockLayer(mimicBlockEntity.getCurrentMimicBlockState()));
		boolean debugRender = ScriptBlocksConfigs.SERVER_CONFIG.enable_mimic_block_debug_mode;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null) {
			debugRender = debugRender || (player.canUseGameMasterBlocks() && player.getInventory().getMainHandStack().isOf(ScriptBlocksBlocks.MIMIC_BLOCK.asItem()));
		}
		blockRenderManager.renderBatched(debugRender ? ScriptBlocksBlocks.MIMIC_FALLBACK_BLOCK.getDefaultState() : mimicBlockEntity.getCurrentMimicBlockState(), mimicBlockEntity.getPos(), mimicBlockEntity.getWorld(), matrixStack, vertexConsumer, true, RandomSource.create());
	}

	@Override
	public int getRenderDistance() {
		return 96;
	}
}

