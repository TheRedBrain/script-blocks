package com.github.theredbrain.scriptblocks.mixin.client.render;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.entity.player.DuckPlayerEntityMixin;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {

	@Shadow
	@Final
	MinecraftClient client;

	@WrapOperation(method = "shouldRenderBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowModifyWorld:Z", opcode = Opcodes.GETFIELD))
	private boolean shouldRenderBlockOutline(PlayerAbilities instance, Operation<Boolean> original) {
		return original.call(instance) && (!(this.client.getCameraEntity() instanceof PlayerEntity playerEntity) || !(playerEntity.hasStatusEffect(ScriptBlocks.ADVENTURE_EFFECT) && !playerEntity.isCreative()));
	}

}
