package com.github.theredbrain.scriptblocks.renderer.blockentity;

import com.github.theredbrain.scriptblocks.block.entity.BoundingBoxesRenderable;
import com.github.theredbrain.scriptblocks.renderer.blockentity.state.BlockEntityWithBoundingBoxesRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BlockEntityWithBoundingBoxesRenderer<T extends BlockEntity & BoundingBoxesRenderable> implements BlockEntityRenderer<T, BlockEntityWithBoundingBoxesRenderState> {
	public static final int STRUCTURE_VOIDS_COLOR = ARGB.colorFromFloat(0.2F, 0.75F, 0.75F, 1.0F);

	public BlockEntityWithBoundingBoxesRenderState createRenderState() {
		return new BlockEntityWithBoundingBoxesRenderState();
	}

	public void extractRenderState(
		final T blockEntity,
		final BlockEntityWithBoundingBoxesRenderState state,
		final float partialTicks,
		final Vec3 cameraPosition,
		final ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		extract(blockEntity, state);
	}

	public static <T extends BlockEntity & BoundingBoxesRenderable> void extract(final T blockEntity, final BlockEntityWithBoundingBoxesRenderState state) {
		LocalPlayer player = Minecraft.getInstance().player;
		state.isVisible = player.canUseGameMasterBlocks() || player.isSpectator();
		state.boundingBoxes = List.of();
		BlockPos entityPos = state.blockPos;

		for (BoundingBoxesRenderable.RenderableBox renderableBox : blockEntity.getRenderableBoxes()) {

			BlockPos pos = renderableBox.localPos();
			Vec3i size = renderableBox.size();
			BlockPos startingPos = entityPos.offset(pos);
			BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType[] invisibleBlocks = new BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType[size.getX() * size.getY() * size.getZ()];
			Boolean[] structureVoids = new Boolean[size.getX() * size.getY() * size.getZ()];
			if (state.isVisible && blockEntity.getLevel() != null && renderableBox.mode() == BoundingBoxesRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS) {

				for (int x = 0; x < size.getX(); x++) {
					for (int y = 0; y < size.getY(); y++) {
						for (int z = 0; z < size.getZ(); z++) {
							int index = z * size.getX() * size.getY() + y * size.getX() + x;
							BlockState blockState = blockEntity.getLevel().getBlockState(startingPos.offset(x, y, z));
							if (blockState.isAir()) {
								invisibleBlocks[index] = BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.AIR;
							} else if (blockState.is(Blocks.STRUCTURE_VOID)) {
								invisibleBlocks[index] = BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.STRUCTURE_VOID;
							} else if (blockState.is(Blocks.BARRIER)) {
								invisibleBlocks[index] = BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.BARRIER;
							} else if (blockState.is(Blocks.LIGHT)) {
								invisibleBlocks[index] = BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.LIGHT;
							}
						}
					}
				}
			} else {
				invisibleBlocks = null;
			}


			if (state.isVisible) {
			}

			structureVoids = null;

			state.boundingBoxes.add(new BlockEntityWithBoundingBoxesRenderState.BoundingBox(renderableBox, invisibleBlocks, structureVoids));
		}
	}

	public void submit(
		final BlockEntityWithBoundingBoxesRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera
	) {
		if (state.isVisible) {
			for (BlockEntityWithBoundingBoxesRenderState.BoundingBox boundingBox : state.boundingBoxes) {
				BoundingBoxesRenderable.RenderableBox renderableBox = boundingBox.renderableBox();
				if (renderableBox.mode() != BoundingBoxesRenderable.Mode.NONE) {
					BlockPos pos = renderableBox.localPos();
					Vec3i size = renderableBox.size();
					if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
						float lineAlpha = 1.0F;
						float lineRGB = 0.9F;
						BlockPos far = pos.offset(size);
						Gizmos.cuboid(
								new AABB(pos.getX(), pos.getY(), pos.getZ(), far.getX(), far.getY(), far.getZ()).move(state.blockPos),
								GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.9F, 0.9F, 0.9F)),
								true
						);
						this.renderInvisibleBlocks(boundingBox.invisibleBlocks(), pos, size);
					}
				}
			}
		}
	}

	private void renderInvisibleBlocks(final BlockEntityWithBoundingBoxesRenderState.@Nullable InvisibleBlockType @Nullable [] invisibleBlocks, final BlockPos startingPos, final Vec3i size) {
		if (invisibleBlocks != null) {

			for (int x = 0; x < size.getX(); x++) {
				for (int y = 0; y < size.getY(); y++) {
					for (int z = 0; z < size.getZ(); z++) {
						int index = z * size.getX() * size.getY() + y * size.getX() + x;
						BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType invisibleBlockType = invisibleBlocks[index];
						if (invisibleBlockType != null) {
							float scale = invisibleBlockType == BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.AIR ? 0.05F : 0.0F;
							double renderX0 = startingPos.getX() + x + 0.45F - scale;
							double renderY0 = startingPos.getY() + y + 0.45F - scale;
							double renderZ0 = startingPos.getZ() + z + 0.45F - scale;
							double renderX1 = startingPos.getX() + x + 0.55F + scale;
							double renderY1 = startingPos.getY() + y + 0.55F + scale;
							double renderZ1 = startingPos.getZ() + z + 0.55F + scale;
							AABB aabb = new AABB(renderX0, renderY0, renderZ0, renderX1, renderY1, renderZ1);
							if (invisibleBlockType == BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.AIR) {
								Gizmos.cuboid(aabb, GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 0.5F, 0.5F, 1.0F)));
							} else if (invisibleBlockType == BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.STRUCTURE_VOID) {
								Gizmos.cuboid(aabb, GizmoStyle.stroke(ARGB.colorFromFloat(1.0F, 1.0F, 0.75F, 0.75F)));
							} else if (invisibleBlockType == BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.BARRIER) {
								Gizmos.cuboid(aabb, GizmoStyle.stroke(-65536));
							} else if (invisibleBlockType == BlockEntityWithBoundingBoxesRenderState.InvisibleBlockType.LIGHT) {
								Gizmos.cuboid(aabb, GizmoStyle.stroke(-256));
							}
						}
					}
				}
			}
		}
	}

	private void renderStructureVoids(final Boolean[] structureVoids, final BlockPos startingPosition, final Vec3i size) {
		if (structureVoids != null) {
			DiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(size.getX(), size.getY(), size.getZ());

			for (int x = 0; x < size.getX(); x++) {
				for (int y = 0; y < size.getY(); y++) {
					for (int z = 0; z < size.getZ(); z++) {
						int index = z * size.getX() * size.getY() + y * size.getX() + x;
						if (structureVoids[index]) {
							shape.fill(x, y, z);
						}
					}
				}
			}

			shape.forAllFaces((direction, xx, yx, zx) -> {
				float scale = 0.48F;
				float x0 = xx + startingPosition.getX() + 0.5F - 0.48F;
				float y0 = yx + startingPosition.getY() + 0.5F - 0.48F;
				float z0 = zx + startingPosition.getZ() + 0.5F - 0.48F;
				float x1 = xx + startingPosition.getX() + 0.5F + 0.48F;
				float y1 = yx + startingPosition.getY() + 0.5F + 0.48F;
				float z1 = zx + startingPosition.getZ() + 0.5F + 0.48F;
				Gizmos.rect(new Vec3(x0, y0, z0), new Vec3(x1, y1, z1), direction, GizmoStyle.fill(STRUCTURE_VOIDS_COLOR));
			});
		}
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 96;
	}
}
