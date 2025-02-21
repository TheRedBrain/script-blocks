package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.EntityRegistry;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class UseRelayBlockEntity extends RotatedBlockEntity {
	public static final BlockPos RELAY_BLOCK_POS_OFFSET_DEFAULT = new BlockPos(0, -1, 0);
	private BlockPos relayBlockPositionOffset = RELAY_BLOCK_POS_OFFSET_DEFAULT;

	public UseRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public UseRelayBlockEntity(BlockPos pos, BlockState state) {
		this(EntityRegistry.USE_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if (this.relayBlockPositionOffset != RELAY_BLOCK_POS_OFFSET_DEFAULT) {
			nbt.putInt("relayBlockPositionOffsetX", this.relayBlockPositionOffset.getX());
			nbt.putInt("relayBlockPositionOffsetY", this.relayBlockPositionOffset.getY());
			nbt.putInt("relayBlockPositionOffsetZ", this.relayBlockPositionOffset.getZ());
		} else {
			nbt.remove("relayBlockPositionOffsetX");
			nbt.remove("relayBlockPositionOffsetY");
			nbt.remove("relayBlockPositionOffsetZ");
		}

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if (nbt.contains("relayBlockPositionOffsetX", NbtElement.INT_TYPE) || nbt.contains("relayBlockPositionOffsetY", NbtElement.INT_TYPE) || nbt.contains("relayBlockPositionOffsetZ", NbtElement.INT_TYPE)) {
			this.relayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetZ"), -48, 48)
			);
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

	public BlockPos getRelayBlockPositionOffset() {
		return this.relayBlockPositionOffset;
	}

	public boolean setRelayBlockPositionOffset(BlockPos relayBlockPositionOffset) {
		if (relayBlockPositionOffset == BlockPos.ORIGIN) {
			return false;
		}

		this.relayBlockPositionOffset = new BlockPos(
				MathHelper.clamp(relayBlockPositionOffset.getX(), -48, 48),
				MathHelper.clamp(relayBlockPositionOffset.getY(), -48, 48),
				MathHelper.clamp(relayBlockPositionOffset.getZ(), -48, 48)
		);
		return true;
	}

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(RotatedBlockWithEntity.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(RotatedBlockWithEntity.ROTATED), this.rotated);
				this.relayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.relayBlockPositionOffset, blockRotation);
				this.rotated = state.get(RotatedBlockWithEntity.ROTATED);
			}
			if (state.get(RotatedBlockWithEntity.X_MIRRORED) != this.x_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(RotatedBlockWithEntity.X_MIRRORED);
			}
			if (state.get(RotatedBlockWithEntity.Z_MIRRORED) != this.z_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(RotatedBlockWithEntity.Z_MIRRORED);
			}
		}
	}
}
