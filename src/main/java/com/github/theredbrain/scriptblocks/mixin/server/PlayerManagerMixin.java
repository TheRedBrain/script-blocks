package com.github.theredbrain.scriptblocks.mixin.server;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@WrapOperation(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
	public @Nullable ServerLevel scriptblocks$wrap_getWorld(MinecraftServer instance, ResourceKey<Level> key, Operation<ServerLevel> original) {
		Identifier worldIdentifier = key.identifier();
		if (worldIdentifier.getNamespace().equals(ScriptBlocks.MOD_ID) && UUIDUtilities.isStringValidUUID(worldIdentifier.getPath())) {
			DimensionsManager.addAndSaveDynamicDimension(worldIdentifier, this.server);
		}
		return original.call(instance, key);
	}
}
