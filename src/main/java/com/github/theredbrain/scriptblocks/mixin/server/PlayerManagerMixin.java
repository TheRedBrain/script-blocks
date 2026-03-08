package com.github.theredbrain.scriptblocks.mixin.server;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@WrapOperation(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
	public @Nullable ServerWorld scriptblocks$wrap_getWorld(MinecraftServer instance, RegistryKey<World> key, Operation<ServerWorld> original) {
		Identifier worldIdentifier = key.getValue();
		if (worldIdentifier.getNamespace().equals(ScriptBlocks.MOD_ID) && UUIDUtilities.isStringValidUUID(worldIdentifier.getPath())) {
			DimensionsManager.addAndSaveDynamicDimension(worldIdentifier, this.server);
		}
		return original.call(instance, key);
	}
}
