package com.github.theredbrain.scriptblocks.mixin.structure.pool;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructurePoolBasedGenerator.class)
public abstract class StructurePoolBasedGeneratorMixin {
	@ModifyExpressionValue(method = "generate(Lnet/minecraft/world/gen/structure/Structure$Context;Lnet/minecraft/registry/entry/RegistryEntry;Ljava/util/Optional;ILnet/minecraft/util/math/BlockPos;ZLjava/util/Optional;ILnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;Lnet/minecraft/world/gen/structure/DimensionPadding;Lnet/minecraft/structure/StructureLiquidSettings;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/structure/Structure$Context;random()Lnet/minecraft/util/math/random/ChunkRandom;"))
	private static ChunkRandom scriptblocks$generate_randomizeChunkRandom(ChunkRandom original) {
		if (!ScriptBlocks.serverConfig.shouldJigSawGenerationBeDeterministic) {
			original.setSeed(Random.create().nextLong()); // this randomizes the jigsaw generation even in the same chunk/position
		}
		return original;
	}

	@ModifyExpressionValue(method = "generate(Lnet/minecraft/world/gen/structure/Structure$Context;Lnet/minecraft/registry/entry/RegistryEntry;Ljava/util/Optional;ILnet/minecraft/util/math/BlockPos;ZLjava/util/Optional;ILnet/minecraft/structure/pool/alias/StructurePoolAliasLookup;Lnet/minecraft/world/gen/structure/DimensionPadding;Lnet/minecraft/structure/StructureLiquidSettings;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/BlockRotation;random(Lnet/minecraft/util/math/random/Random;)Lnet/minecraft/util/BlockRotation;"))
	private static BlockRotation scriptblocks$generate_removeRandomBlockRotation(BlockRotation original) {
		if (!ScriptBlocks.serverConfig.shouldJigSawStructuresBeRandomlyRotated) {
			return BlockRotation.NONE; // this sets the initial rotation to always be the same
		}
		return original;
	}
}
