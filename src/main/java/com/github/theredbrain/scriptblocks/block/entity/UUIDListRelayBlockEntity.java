package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.HandlesUUIDList;
import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UUIDListRelayBlockEntity extends RotatedBlockEntity implements HandlesUUIDList {
	private BlockPos uuidListHandlerPositionOffset = new BlockPos(0, 1, 0);

	public UUIDListRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("uuid_list_handler_position_offset_x", this.uuidListHandlerPositionOffset.getX());
		nbt.putInt("uuid_list_handler_position_offset_y", this.uuidListHandlerPositionOffset.getY());
		nbt.putInt("uuid_list_handler_position_offset_z", this.uuidListHandlerPositionOffset.getZ());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		this.uuidListHandlerPositionOffset = new BlockPos(
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_x"), -48, 48),
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_y"), -48, 48),
				MathHelper.clamp(nbt.getInt("uuid_list_handler_position_offset_z"), -48, 48)
		);

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
	public BlockPos getUuidListHandlerPositionOffset() {
		return this.uuidListHandlerPositionOffset;
	}

	public void setUuidListHandlerPositionOffset(BlockPos uuidListHandlerPositionOffset) {
		this.uuidListHandlerPositionOffset = uuidListHandlerPositionOffset;
	}
	// endregion --- getter & setter ---

	public BlockPos getActualUUIDListHandlerPosition() {
		return new BlockPos(this.pos.getX() + this.uuidListHandlerPositionOffset.getX(), this.pos.getY() + this.uuidListHandlerPositionOffset.getY(), this.pos.getZ() + this.uuidListHandlerPositionOffset.getZ());
	}

	@Override
	public List<UUID> supplyUUIDList(boolean remove) {
		if (this.world == null) {
			return new ArrayList<>();
		}
		BlockEntity blockEntity = world.getBlockEntity(this.getActualUUIDListHandlerPosition());
		if (blockEntity == this) {
			return new ArrayList<>();
		}
		if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
			return handlesUUIDList.supplyUUIDList(remove);
		}
		return new ArrayList<>();
	}

	@Override
	public void handleUUIDList(List<UUID> list, boolean remove) {
		if (this.world == null) {
			return;
		}
		BlockEntity blockEntity = world.getBlockEntity(this.getActualUUIDListHandlerPosition());
		if (blockEntity == this) {
			return;
		}
		if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
			handlesUUIDList.handleUUIDList(list, remove);
		}
	}

	@Override
	public void reset() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(this.getActualUUIDListHandlerPosition());
			if (blockEntity == this) {
				return;
			}
			if (blockEntity instanceof HandlesUUIDList handlesUUIDList) {
				handlesUUIDList.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.uuidListHandlerPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListHandlerPositionOffset, blockRotation);
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.uuidListHandlerPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListHandlerPositionOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

}
