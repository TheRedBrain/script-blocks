package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.block.Triggerable;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksNBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
	protected void saveAdditional(ValueOutput view) {

		CompoundTag nbtCompound = new CompoundTag();
		nbtCompound.putBoolean("visible", this.showArea);
		nbtCompound.store("position_offset", BlockPos.CODEC, this.areaPositionOffset);
		nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(this.areaDimensions));
		view.store("area", CompoundTag.CODEC, nbtCompound.copy());

		ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("sub_areas", CompoundTag.CODEC);
		for (MutablePair<BlockPos, Vec3i> subArea : this.subAreasList) {
			nbtCompound = new CompoundTag();
			nbtCompound.store("position_offset", BlockPos.CODEC, subArea.getLeft());
			nbtCompound.put("dimensions", ScriptBlocksNBTHelper.fromVec3i(subArea.getRight()));
			listAppender.add(nbtCompound.copy());
		}

		view.putString("block_identifier_string", this.blockIdentifierString);

		super.saveAdditional(view);

	}

	@Override
	protected void loadAdditional(ValueInput view) {

		CompoundTag nbtCompound = view.read("area", CompoundTag.CODEC).orElseGet(CompoundTag::new);
		this.showArea = nbtCompound.getBoolean("visible").orElse(false);

		this.areaDimensions = ScriptBlocksNBTHelper.toVec3i(nbtCompound, "dimensions").orElse(new Vec3i(0, 0, 0));

		this.areaPositionOffset = BlockPos.max(
				BlockPos.min(
						nbtCompound.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		this.subAreasList.clear();
		for (CompoundTag nbtCompound1 : view.listOrEmpty("sub_areas", CompoundTag.CODEC)) {
			this.subAreasList.add(new MutablePair<>(
					nbtCompound1.read("position_offset", BlockPos.CODEC).orElse(BlockPos.ZERO),
					ScriptBlocksNBTHelper.toVec3i(nbtCompound1, "dimensions").orElse(new Vec3i(0, 0, 0))
			));
		}

		this.blockIdentifierString = view.getStringOr("block_identifier_string", "");

		super.loadAdditional(view);
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return this.saveCustomOnly(registryLookup);
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
		if (this.level != null) {
			Optional<Holder.Reference<Block>> optionalBlockReference = this.level.registryAccess().get(Registries.BLOCK).getEntry(Identifier.tryParse(blockIdentifierString));
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

		if (this.level instanceof ServerLevel serverWorld) {
			MinecraftServer server = serverWorld.getServer();

			// calculate sub areas
			if (this.subAreasList.isEmpty()) {
				this.subAreasList.addAll(splitArea(this.areaPositionOffset, this.areaDimensions, serverWorld.getGameRules().get(GameRules.MAX_BLOCK_MODIFICATIONS), 16)); // TODO maxIterations gamerule or config
				this.setChanged();
			}

			BlockPos startPos;
			BlockPos endPos;
			// fill each sub area with the block
			for (MutablePair<BlockPos, Vec3i> pair : this.subAreasList) {

				startPos = this.worldPosition.offset(pair.getLeft());
				endPos = startPos.offset(pair.getRight().getX() - 1, pair.getRight().getY() - 1, pair.getRight().getZ() - 1);
				String commandString = "/execute in " + this.level.dimension().identifier() + " run fill " + startPos.getX() + " " + startPos.getY() + " " + startPos.getZ() + " " + endPos.getX() + " " + endPos.getY() + " " + endPos.getZ() + " " + this.blockIdentifierString;
				ScriptBlocks.info(commandString);
				server.getCommands().executeWithPrefix(server.createCommandSourceStack(), commandString);
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
				list.addAll(splitArea(areaPos.offset(second_pos_offset[0], second_pos_offset[1], second_pos_offset[2]), new Vec3i(new_array_2[0], new_array_2[1], new_array_2[2]), maxVolume, maxIterations - 1));
			}
		} else if (this.level != null) {
			ScriptBlocks.info("AreaFillerBlock in " + this.level.dimension().identifier() + " at " + this.worldPosition.toShortString() + " ran out of iteration attempts!");
		}
		return list;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.getValue(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				Rotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.getValue(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.rotateOffsetArea(this.areaPositionOffset, this.areaDimensions, blockRotation);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.rotated = state.getValue(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.FRONT_BACK);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.x_mirrored = state.getValue(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				MutablePair<BlockPos, Vec3i> offsetArea = BlockRotationUtils.mirrorOffsetArea(this.areaPositionOffset, this.areaDimensions, Mirror.LEFT_RIGHT);
				this.areaPositionOffset = offsetArea.getLeft();
				this.areaDimensions = offsetArea.getRight();

				this.z_mirrored = state.getValue(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
			this.subAreasList.clear();
		}
	}
}
