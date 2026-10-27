package com.github.theredbrain.scriptblocks.util;

import java.util.Optional;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;

public class ScriptBlocksNBTHelper {

	public static Optional<Vec3i> toVec3i(CompoundTag nbt, String key) {
		Optional<int[]> optional = nbt.getIntArray(key);
		return (optional.isPresent() && optional.get().length == 3) ? Optional.of(new Vec3i(optional.get()[0], optional.get()[1], optional.get()[2])) : Optional.empty();
	}

	public static Tag fromVec3i(Vec3i pos) {
		return new IntArrayTag(new int[]{pos.getX(), pos.getY(), pos.getZ()});
	}

}
