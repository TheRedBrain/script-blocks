package com.github.theredbrain.scriptblocks.renderer.blockentity.state;

import com.github.theredbrain.scriptblocks.block.entity.BoundingBoxesRenderable;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BlockEntityWithBoundingBoxesRenderState extends BlockEntityRenderState {
	public boolean isVisible;
	public List<BoundingBox> boundingBoxes;
//	public List<BlockEntityWithBoundingBoxesRenderState.@Nullable InvisibleBlockType @Nullable []> invisibleBlocks;
//	public List<Boolean[]> structureVoids;

	public enum InvisibleBlockType {
		AIR,
		BARRIER,
		LIGHT,
		STRUCTURE_VOID;
	}

	public record BoundingBox(
			BoundingBoxesRenderable.RenderableBox renderableBox,
			BlockEntityWithBoundingBoxesRenderState.@Nullable InvisibleBlockType @Nullable [] invisibleBlocks,
			Boolean[] structureVoids
	) {

	}
}
