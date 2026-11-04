package com.github.theredbrain.scriptblocks.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;

public interface BoundingBoxesRenderable {
	default List<BoundingBoxesRenderable.RenderableBox> getRenderableBoxes() {
		return List.of();
	}

	enum Mode {
		NONE,
		BOX,
		BOX_AND_INVISIBLE_BLOCKS;
	}

	record RenderableBox(BlockPos localPos, Vec3i size, Mode mode) {
		public static BoundingBoxesRenderable.RenderableBox fromCorners(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, Mode mode) {
			int x = Math.min(x1, x2);
			int y = Math.min(y1, y2);
			int z = Math.min(z1, z2);
			return new BoundingBoxesRenderable.RenderableBox(new BlockPos(x, y, z), new Vec3i(Math.max(x1, x2) - x, Math.max(y1, y2) - y, Math.max(z1, z2) - z), mode);
		}
	}
}
