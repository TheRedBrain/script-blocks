package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.ProvidesUUIDList;
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

public class UUIDListRelayBlockEntity extends RotatedBlockEntity implements ProvidesUUIDList {
	private BlockPos uuidListProviderPositionOffset = new BlockPos(0, 1, 0);

	public UUIDListRelayBlockEntity(BlockPos pos, BlockState state) {
		super(ScriptBlocksEntities.UUID_LIST_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.putInt("uuid_list_provider_position_offset_x", this.uuidListProviderPositionOffset.getX());
		nbt.putInt("uuid_list_provider_position_offset_y", this.uuidListProviderPositionOffset.getY());
		nbt.putInt("uuid_list_provider_position_offset_z", this.uuidListProviderPositionOffset.getZ());

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		int i;
		int j;
		int k;
			i = MathHelper.clamp(nbt.getInt("uuid_list_provider_position_offset_x"), -48, 48);
			j = MathHelper.clamp(nbt.getInt("uuid_list_provider_position_offset_y"), -48, 48);
			k = MathHelper.clamp(nbt.getInt("uuid_list_provider_position_offset_z"), -48, 48);
		this.uuidListProviderPositionOffset = new BlockPos(i, j, k);

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
	public BlockPos getUuidListProviderPositionOffset() {
		return this.uuidListProviderPositionOffset;
	}

	public void setUuidListProviderPositionOffset(BlockPos uuidListProviderPositionOffset) {
		this.uuidListProviderPositionOffset = uuidListProviderPositionOffset;
	}
	// endregion --- getter & setter ---

	@Override
	public List<UUID> getUUIDList(boolean remove) {
		if (this.world == null) {
			return new ArrayList<>();
		}
		BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.uuidListProviderPositionOffset.getX(), this.pos.getY() + this.uuidListProviderPositionOffset.getY(), this.pos.getZ() + this.uuidListProviderPositionOffset.getZ()));
		if (blockEntity == this) {
			return new ArrayList<>();
		}
		if (blockEntity instanceof ProvidesUUIDList providesUUIDList) {
			return providesUUIDList.getUUIDList(remove);
		}
		return new ArrayList<>();
	}

	@Override
	public void modifyUUIDList(List<UUID> list, boolean remove) {
		if (this.world == null) {
			return;
		}
		BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.uuidListProviderPositionOffset.getX(), this.pos.getY() + this.uuidListProviderPositionOffset.getY(), this.pos.getZ() + this.uuidListProviderPositionOffset.getZ()));
		if (blockEntity == this) {
			return;
		}
		if (blockEntity instanceof ProvidesUUIDList providesUUIDList) {
			providesUUIDList.modifyUUIDList(list, remove);
		}
	}

	@Override
	public void reset() {
		if (this.world != null) {
			BlockEntity blockEntity = world.getBlockEntity(new BlockPos(this.pos.getX() + this.uuidListProviderPositionOffset.getX(), this.pos.getY() + this.uuidListProviderPositionOffset.getY(), this.pos.getZ() + this.uuidListProviderPositionOffset.getZ()));
			if (blockEntity == this) {
				return;
			}
			if (blockEntity instanceof ProvidesUUIDList providesUUIDList) {
				providesUUIDList.reset();
			}
		}
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.uuidListProviderPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.uuidListProviderPositionOffset, blockRotation);
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.uuidListProviderPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListProviderPositionOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.uuidListProviderPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.uuidListProviderPositionOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}

}
