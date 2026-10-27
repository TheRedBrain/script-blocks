package com.github.theredbrain.scriptblocks.mixin.client.render;

import com.github.theredbrain.scriptblocks.registry.ScriptBlocksStatusEffects;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
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
	Minecraft client;

	@WrapOperation(method = "shouldRenderBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerAbilities;allowModifyWorld:Z", opcode = Opcodes.GETFIELD))
	private boolean shouldRenderBlockOutline(Abilities instance, Operation<Boolean> original) {
		return original.call(instance) && (!(this.client.getCameraEntity() instanceof Player playerEntity) || !(playerEntity.hasEffect(ScriptBlocksStatusEffects.ADVENTURE_EFFECT) && !playerEntity.isCreative()));
	}

}
