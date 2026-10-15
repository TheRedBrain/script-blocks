package com.github.theredbrain.scriptblocks.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;

public class ScriptBlocksNBTHelper {

	public static Optional<Vec3i> toVec3i(NbtCompound nbt, String key) {
		int[] is = nbt.getIntArray(key);
		return is.length == 3 ? Optional.of(new Vec3i(is[0], is[1], is[2])) : Optional.empty();
	}

	public static NbtElement fromVec3i(Vec3i pos) {
		return new NbtIntArray(new int[]{pos.getX(), pos.getY(), pos.getZ()});
	}

}
