package com.github.theredbrain.scriptblocks.mixin.server;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.UUIDUtilities;
import com.github.theredbrain.scriptblocks.world.DimensionsManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Shadow
	@Final
	private MinecraftServer server;

	@WrapOperation(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;level()Lnet/minecraft/server/level/ServerLevel;"))
	public ServerLevel scriptblocks$wrap_level(ServerPlayer instance, Operation<ServerLevel> original) {
		Identifier levelIdentifier = instance.level().dimension().identifier();
		if (levelIdentifier.getNamespace().equals(ScriptBlocks.MOD_ID) && UUIDUtilities.isStringValidUUID(levelIdentifier.getPath())) {
			DimensionsManager.addAndSaveDynamicDimension(levelIdentifier, this.server);
		}
		return original.call(instance);
	}
}
