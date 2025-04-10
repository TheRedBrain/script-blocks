package com.github.theredbrain.scriptblocks.mixin.client.render.item;

import com.github.theredbrain.scriptblocks.block.entity.AestheticDecoratedPotBlockEntity;
import com.github.theredbrain.scriptblocks.registry.BlockRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltinModelItemRenderer.class)
public class BuiltinModelItemRendererMixin {

	@Shadow
	@Final
	private BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	@Unique
	private final AestheticDecoratedPotBlockEntity renderDecoratedPot = new AestheticDecoratedPotBlockEntity(BlockPos.ORIGIN, BlockRegistry.AESTHETIC_DECORATED_POT.getDefaultState());

	@Inject(method = "render(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II)V", at = @At("HEAD"), cancellable = true)
	private void scriptblocks$render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, CallbackInfo ci) {
		if (stack.isOf(BlockRegistry.AESTHETIC_DECORATED_POT.asItem())) {
			renderDecoratedPot.readFrom(stack);
			this.blockEntityRenderDispatcher.renderEntity(renderDecoratedPot, matrices, vertexConsumers, light, overlay);
			ci.cancel();
		}
	}

}
