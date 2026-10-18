package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UUIDListRelayBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private final List<BlockPos> uuidListHandlers = new ArrayList<>();

	public UUIDListRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		NbtList nbtList = new NbtList();
		for (BlockPos blockPos : this.uuidListHandlers) {
			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.put("position_offset", NbtHelper.fromBlockPos(blockPos));
			nbtList.add(nbtCompound.copy());
		}
		nbt.put("uuid_list_handlers", nbtList);

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.uuidListHandlers.clear();
		if (nbt.contains("uuid_list_handlers_size")) {
			int x;
			int y;
			int z;
			int uuidListHandlersSize = nbt.getInt("uuid_list_handlers_size");
			for (int i = 0; i < uuidListHandlersSize; i++) {
				x = MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_x_" + i), -48, 48);
				y = MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_y_" + i), -48, 48);
				z = MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_z_" + i), -48, 48);
				this.uuidListHandlers.add(new BlockPos(x, y, z));
				nbt.remove("uuid_list_handler_position_offset_x_" + i);
				nbt.remove("uuid_list_handler_position_offset_y_" + i);
				nbt.remove("uuid_list_handler_position_offset_z_" + i);
			}
			nbt.remove("uuid_list_handlers_size");
		} else {
			NbtList nbtList = nbt.getList("uuid_list_handlers", NbtElement.COMPOUND_TYPE);
			for (int i = 0; i < nbtList.size(); i++) {
				this.uuidListHandlers.add(
						BlockPos.max(
								BlockPos.min(
										NbtHelper.toBlockPos(nbtList.getCompound(i), "position_offset").orElse(BlockPos.ORIGIN),
										new BlockPos(48, 48, 48)
								),
								new BlockPos(-48, -48, -48)
						)
				);
			}
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
	public List<BlockPos> getUuidListHandlers() {
		return this.uuidListHandlers;
	}

	public void setUuidListHandlers(List<BlockPos> uuidListHandlers) {
		this.uuidListHandlers.clear();
		this.uuidListHandlers.addAll(uuidListHandlers);
	}
	// endregion --- getter & setter ---

	public BlockPos getActualUUIDListHandlerPosition(BlockPos uuidListHandler) {
		return new BlockPos(this.pos.getX() + uuidListHandler.getX(), this.pos.getY() + uuidListHandler.getY(), this.pos.getZ() + uuidListHandler.getZ());
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		if (this.getWorld() == null) {
			return new ArrayList<>();
		}
		BlockEntity blockEntity;
		Set<UUID> newUuidList = new HashSet<>();
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getWorld().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				continue;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				newUuidList.addAll(handlesUUIDList.supplyUUIDList(remove));
			}
		}
		return newUuidList.stream().toList();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.getWorld() == null) {
			return;
		}
		BlockEntity blockEntity;
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getWorld().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				return;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.handleUUIDList(list, remove);
			}
		}
	}

	@Override
	public void reset() {
		if (this.getWorld() == null) {
			return;
		}
		BlockEntity blockEntity;
		for (BlockPos uuidListHandler : this.uuidListHandlers) {
			blockEntity = this.getWorld().getBlockEntity(getActualUUIDListHandlerPosition(uuidListHandler));
			if (blockEntity == this) {
				continue;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.rotateOffsetBlockPos(uuidListHandler, blockRotation));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.mirrorOffsetBlockPos(uuidListHandler, BlockMirror.FRONT_BACK));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {

				List<BlockPos> newUuidListHandlers = new ArrayList<>();
				for (BlockPos uuidListHandler : this.uuidListHandlers) {
					newUuidListHandlers.add(BlockRotationUtils.mirrorOffsetBlockPos(uuidListHandler, BlockMirror.LEFT_RIGHT));
				}
				this.uuidListHandlers.clear();
				this.uuidListHandlers.addAll(newUuidListHandlers);

				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}

}
