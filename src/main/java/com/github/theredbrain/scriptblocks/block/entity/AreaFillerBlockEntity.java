package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
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
	private List<MutablePair<BlockPos, Vec3i>> subAreasList = new ArrayList<>();

	public AreaFillerBlockEntity(BlockPos pos, BlockState state) {
		super(EntityRegistry.AREA_FILLER_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		if (this.showArea) {
			nbt.putBoolean("showArea", true);
		} else {
			nbt.remove("showArea");
		}

		if (this.areaDimensions.getX() != 0) {
			nbt.putInt("areaDimensionsX", this.areaDimensions.getX());
		} else {
			nbt.remove("areaDimensionsX");
		}

		if (this.areaDimensions.getY() != 0) {
			nbt.putInt("areaDimensionsY", this.areaDimensions.getY());
		} else {
			nbt.remove("areaDimensionsY");
		}

		if (this.areaDimensions.getZ() != 0) {
			nbt.putInt("areaDimensionsZ", this.areaDimensions.getZ());
		} else {
			nbt.remove("areaDimensionsZ");
		}

		if (this.areaPositionOffset.getX() != 0) {
			nbt.putInt("areaPositionOffsetX", this.areaPositionOffset.getX());
		} else {
			nbt.remove("areaPositionOffsetX");
		}

		if (this.areaPositionOffset.getY() != 0) {
			nbt.putInt("areaPositionOffsetY", this.areaPositionOffset.getY());
		} else {
			nbt.remove("areaPositionOffsetY");
		}

		if (this.areaPositionOffset.getZ() != 0) {
			nbt.putInt("areaPositionOffsetZ", this.areaPositionOffset.getZ());
		} else {
			nbt.remove("areaPositionOffsetZ");
		}

		if (!this.subAreasList.isEmpty()) {
			int previousSize = nbt.getInt("subAreasListSize");
			nbt.putInt("subAreasListSize", subAreasList.size());
			for (int i = 0; i < Math.max(this.subAreasList.size(), previousSize); i++) {
				if (i < this.subAreasList.size()) {
					BlockPos areaPositionOffset = this.subAreasList.get(i).getLeft();
					Vec3i areaDimension = this.subAreasList.get(i).getRight();
					nbt.putInt("subAreaPositionOffsetX_" + i, areaPositionOffset.getX());
					nbt.putInt("subAreaPositionOffsetY_" + i, areaPositionOffset.getY());
					nbt.putInt("subAreaPositionOffsetZ_" + i, areaPositionOffset.getZ());
					nbt.putInt("subAreaDimensionX_" + i, areaDimension.getX());
					nbt.putInt("subAreaDimensionY_" + i, areaDimension.getY());
					nbt.putInt("subAreaDimensionZ_" + i, areaDimension.getZ());
				} else {
					nbt.remove("subAreaPositionOffsetX_" + i);
					nbt.remove("subAreaPositionOffsetY_" + i);
					nbt.remove("subAreaPositionOffsetZ_" + i);
					nbt.remove("subAreaDimensionX_" + i);
					nbt.remove("subAreaDimensionY_" + i);
					nbt.remove("subAreaDimensionZ_" + i);
				}
			}
		}

		if (!this.blockIdentifierString.isEmpty()) {
			nbt.putString("blockIdentifierString", this.blockIdentifierString);
		} else {
			nbt.remove("blockIdentifierString");
		}

		super.writeNbt(nbt, registryLookup);

	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.showArea = nbt.getBoolean("showArea");

		int i = Math.max(0, nbt.getInt("areaDimensionsX"));
		int j = Math.max(0, nbt.getInt("areaDimensionsY"));
		int k = Math.max(0, nbt.getInt("areaDimensionsZ"));
		this.areaDimensions = new Vec3i(i, j, k);

		int l = nbt.getInt("areaPositionOffsetX");
		int m = nbt.getInt("areaPositionOffsetY");
		int n = nbt.getInt("areaPositionOffsetZ");
		this.areaPositionOffset = new BlockPos(l, m, n);

		int subAreasListSize = nbt.getInt("subAreasListSize");
		this.subAreasList = new ArrayList<>(List.of());
		for (i = 0; i < subAreasListSize; i++) {
			this.subAreasList.add(new MutablePair<>(new BlockPos(
					nbt.getInt("subAreaPositionOffsetX_" + i),
					nbt.getInt("subAreaPositionOffsetY_" + i),
					nbt.getInt("subAreaPositionOffsetZ_" + i)
			), new Vec3i(
					nbt.getInt("subAreaDimensionX_" + i),
					nbt.getInt("subAreaDimensionY_" + i),
					nbt.getInt("subAreaDimensionZ_" + i)
			)));
		}

		this.blockIdentifierString = nbt.getString("blockIdentifierString");

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

	// TODO check if input is valid
	public boolean setAreaDimensions(Vec3i areaDimensions) {
		this.areaDimensions = areaDimensions;
		this.subAreasList.clear();
		return true;
	}

	public BlockPos getAreaPositionOffset() {
		return areaPositionOffset;
	}

	// TODO check if input is valid
	public boolean setAreaPositionOffset(BlockPos areaPositionOffset) {
		this.areaPositionOffset = areaPositionOffset;
		this.subAreasList.clear();
		return true;
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
				this.subAreasList = splitArea(this.areaPositionOffset, this.areaDimensions, server.getGameRules().get(GameRules.COMMAND_MODIFICATION_BLOCK_LIMIT).get(), 16); // TODO maxIterations gamerule or config
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
