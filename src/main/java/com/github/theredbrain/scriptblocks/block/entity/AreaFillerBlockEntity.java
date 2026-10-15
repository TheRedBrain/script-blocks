package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameRules;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AreaFillerBlockEntity extends RotatedBlockEntity implements Triggerable {

	private boolean showArea = false;
	private Vec3i areaDimensions = Vec3i.ZERO;
	private BlockPos areaPositionOffset = new BlockPos(0, 1, 0);
	private String blockIdentifierString = "";
	private final List<MutablePair<BlockPos, Vec3i>> subAreasList = new ArrayList<>();

	public AreaFillerBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.AREA_FILLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = new NbtCompound();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.put("position_offset", NbtHelper.fromBlockPos(this.areaPositionOffset));
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		nbt.put("area", nbtCompound.copy());

		NbtList nbtList = new NbtList();
		for (MutablePair<BlockPos, Vec3i> subArea : this.subAreasList) {
			nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", NbtHelper.fromBlockPos(subArea.getLeft()));
			nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(subArea.getRight()));
			nbtList.add(nbtCompound.copy());
		}
		nbt.put("sub_areas", nbtList);

		nbt.putString("block_identifier_string", this.blockIdentifierString);

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtCompound nbtCompound = nbt.getCompound("area");
		if (nbt.contains("showArea")) {
			this.showArea = nbt.getBoolean("showArea");
			nbt.remove("showArea");
		} else if (nbt.contains("show_area")) {
			this.showArea = nbt.getBoolean("show_area");
			nbt.remove("show_area");
		} else {
			this.showArea = nbtCompound.getBoolean("visible");
		}

		if (nbt.contains("areaDimensionsX") || nbt.contains("areaDimensionsY") || nbt.contains("areaDimensionsZ")) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("areaDimensionsX"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsY"), 0, 48),
					MathHelper.clamp(nbt.getInt("areaDimensionsZ"), 0, 48)
			);
			nbt.remove("areaDimensionsX");
			nbt.remove("areaDimensionsY");
			nbt.remove("areaDimensionsZ");
		} else if (nbt.contains("area_dimensions_x") || nbt.contains("area_dimensions_y") || nbt.contains("area_dimensions_z")) {
			this.areaDimensions = new Vec3i(
					MathHelper.clamp(nbt.getInt("area_dimensions_x"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_y"), 0, 48),
					MathHelper.clamp(nbt.getInt("area_dimensions_z"), 0, 48)
			);
			nbt.remove("area_dimensions_x");
			nbt.remove("area_dimensions_y");
			nbt.remove("area_dimensions_z");
		} else {
			this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));
		}

		if (nbt.contains("areaPositionOffsetX") || nbt.contains("areaPositionOffsetY") || nbt.contains("areaPositionOffsetZ")) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("areaPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("areaPositionOffsetZ"), -48, 48)
			);
			nbt.remove("areaPositionOffsetX");
			nbt.remove("areaPositionOffsetY");
			nbt.remove("areaPositionOffsetZ");
		} else if (nbt.contains("area_position_offset_x") || nbt.contains("area_position_offset_y") || nbt.contains("area_position_offset_z")) {
			this.areaPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("area_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("area_position_offset_z"), -48, 48)
			);
			nbt.remove("area_position_offset_x");
			nbt.remove("area_position_offset_y");
			nbt.remove("area_position_offset_z");
		} else {
			this.areaPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
			);
		}

		this.subAreasList.clear();
		if (nbt.contains("subAreasListSize")) {
			int subAreasListSize = nbt.getInt("subAreasListSize");
			for (int i = 0; i < subAreasListSize; i++) {
				this.subAreasList.add(new MutablePair<>(new BlockPos(
						nbt.getInt("subAreaPositionOffsetX_" + i),
						nbt.getInt("subAreaPositionOffsetY_" + i),
						nbt.getInt("subAreaPositionOffsetZ_" + i)
				), new Vec3i(
						nbt.getInt("subAreaDimensionX_" + i),
						nbt.getInt("subAreaDimensionY_" + i),
						nbt.getInt("subAreaDimensionZ_" + i)
				)));
				nbt.remove("subAreaPositionOffsetX_" + i);
				nbt.remove("subAreaPositionOffsetY_" + i);
				nbt.remove("subAreaPositionOffsetZ_" + i);
				nbt.remove("subAreaDimensionX_" + i);
				nbt.remove("subAreaDimensionY_" + i);
				nbt.remove("subAreaDimensionZ_" + i);
			}
			nbt.remove("subAreasListSize");
		} else if (nbt.contains("sub_areas_list_size")) {
			int subAreasListSize = nbt.getInt("sub_areas_list_size");
			for (int i = 0; i < subAreasListSize; i++) {
				this.subAreasList.add(new MutablePair<>(new BlockPos(
						nbt.getInt("sub_area_position_offset_x_" + i),
						nbt.getInt("sub_area_position_offset_y_" + i),
						nbt.getInt("sub_area_position_offset_z_" + i)
				), new Vec3i(
						nbt.getInt("sub_area_dimension_x_" + i),
						nbt.getInt("sub_area_dimension_y_" + i),
						nbt.getInt("sub_area_dimension_z_" + i)
				)));
				nbt.remove("sub_area_position_offset_x_" + i);
				nbt.remove("sub_area_position_offset_y_" + i);
				nbt.remove("sub_area_position_offset_z_" + i);
				nbt.remove("sub_area_dimension_x_" + i);
				nbt.remove("sub_area_dimension_y_" + i);
				nbt.remove("sub_area_dimension_z_" + i);
			}
			nbt.remove("sub_areas_list_size");
		} else {
			NbtList nbtList = nbt.getList("sub_areas", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				nbtCompound = nbtList.getCompound(i);
				this.subAreasList.add(new MutablePair<>(
						NbtHelper.toBlockPos(nbtCompound, "position_offset").orElse(BlockPos.ORIGIN),
						ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0))
				));
			}
		}

		if (nbt.contains("blockIdentifierString")) {
			this.blockIdentifierString = nbt.getString("blockIdentifierString");
			nbt.remove("blockIdentifierString");
		} else {
			this.blockIdentifierString = nbt.getString("block_identifier_string");
		}

		super.readNbt(nbt, registryLookup);
	}

	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createComponentlessNbt(registryLookup);
	}

	// region --- getter & setter ---
	public boolean showArea() {
		return showArea;
	}

	public void setShowArea(boolean showArea) {
		this.showArea = showArea;
	}

	public Vec3i getAreaDimensions() {
		return areaDimensions;
	}

	public void setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.subAreasList.clear();
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	public void setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.subAreasList.clear();
	}

	public String getBlockIdentifierString() {
		return this.blockIdentifierString;
	}

	public boolean setBlockIdentifierString(String blockIdentifierString) {
		if (this.world != null) {
			Optional<RegistryEntry.Reference<Block>> optionalBlockReference = this.world.getRegistryManager().get(RegistryKeys.BLOCK).getEntry(Identifier.tryParse(blockIdentifierString));
			if (optionalBlockReference.isPresent()) {
				this.blockIdentifierString = blockIdentifierString;
				return true;
			}
		}
		return false;
	}
	// endregion --- getter & setter ---

	@Override
	public void trigger() {

		if (this.world instanceof ServerWorld serverWorld) {
			MinecraftServer server = serverWorld.getServer();

			// calculate sub areas
			if (this.subAreasList.isEmpty()) {
				this.subAreasList.addAll(splitArea(this.areaPositionOffset, this.areaDimensions, server.getGameRules().get(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT).get(), 16)); // TODO maxIterations gamerule or config
				this.markDirty();
			}

			BlockPos startPos;
			BlockPos endPos;
			// fill each sub area with the block
			for (MutablePair<BlockPos, Vec3i> pair : this.subAreasList) {

				startPos = this.pos.add(pair.getLeft());
				endPos = startPos.add(pair.getRight().getX() - 1, pair.getRight().getY() - 1, pair.getRight().getZ() - 1);
				String commandString = "/execute in " + this.world.getRegistryKey().getValue() + " run fill " + startPos.getX() + " " + startPos.getY() + " " + startPos.getZ() + " " + endPos.getX() + " " + endPos.getY() + " " + endPos.getZ() + " " + this.blockIdentifierString;
				ScriptBlocks.info(commandString);
				server.getCommandManager().executeWithPrefix(server.getCommandSource(), commandString);
			}
		}
	}

	private List<MutablePair<BlockPos, Vec3i>> splitArea(BlockPos areaPos, Vec3i areaDimensions, int maxVolume, int maxIterations) {
		List<MutablePair<BlockPos, Vec3i>> list = new ArrayList<>();

		if (maxIterations > 0) {
			int x = areaDimensions.getX();
			int y = areaDimensions.getY();
			int z = areaDimensions.getZ();
			if (x * y * z <= maxVolume) {
				list.add(new MutablePair<>(areaPos, areaDimensions));
			} else {

				int m = x;
				int index = 0;
				if (y > m) {
					m = y;
					index = 1;
				}
				if (z > m) {
					m = z;
					index = 2;
				}

				int n;
				if (m % 2 == 0) {
					m = m / 2;
					n = m;
				} else {
					m = (int) Math.ceil((double) m / 2);
					n = m - 1;
				}

				int[] new_array_1 = {x, y, z};
				int[] new_array_2 = {x, y, z};
				int[] second_pos_offset = {0, 0, 0};
				new_array_1[index] = m;
				new_array_2[index] = n;
				second_pos_offset[index] = m;

				list.addAll(splitArea(areaPos, new Vec3i(new_array_1[0], new_array_1[1], new_array_1[2]), maxVolume, maxIterations - 1));
				list.addAll(splitArea(areaPos.add(second_pos_offset[0], second_pos_offset[1], second_pos_offset[2]), new Vec3i(new_array_2[0], new_array_2[1], new_array_2[2]), maxVolume, maxIterations - 1));
			}
		} else if (this.world != null) {
			ScriptBlocks.info("AreaFillerBlock in " + this.world.getRegistryKey().getValue() + " at " + this.pos.toShortString() + " ran out of iteration attempts!");
		}
		return list;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, BlockMirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
			this.subAreasList.clear();
		}
	}
}
