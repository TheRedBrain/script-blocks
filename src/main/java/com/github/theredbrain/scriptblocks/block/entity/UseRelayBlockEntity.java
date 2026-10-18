package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class UseRelayBlockEntity extends RotatedBlockEntity {
	protected BlockPos relayBlockPositionOffset = BlockPos.ORIGIN;

	public UseRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public UseRelayBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.USE_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {

		nbt.put("relay_block_position_offset", NbtHelper.fromBlockPos(this.relayBlockPositionOffset));

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if (nbt.contains("relayBlockPositionOffsetX") || nbt.contains("relayBlockPositionOffsetY") || nbt.contains("relayBlockPositionOffsetZ")) {
			this.relayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetX"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetY"), -48, 48),
					MathHelper.clamp(nbt.getInt("relayBlockPositionOffsetZ"), -48, 48)
			);
			nbt.remove("relayBlockPositionOffsetX");
			nbt.remove("relayBlockPositionOffsetY");
			nbt.remove("relayBlockPositionOffsetZ");
		} else if (nbt.contains("relay_block_position_offset_x") || nbt.contains("relay_block_position_offset_y") || nbt.contains("relay_block_position_offset_z")) {
			this.relayBlockPositionOffset = new BlockPos(
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_x"), -48, 48),
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_y"), -48, 48),
					MathHelper.clamp(nbt.getInt("relay_block_position_offset_z"), -48, 48)
			);
			nbt.remove("relay_block_position_offset_x");
			nbt.remove("relay_block_position_offset_y");
			nbt.remove("relay_block_position_offset_z");
		} else {
			this.relayBlockPositionOffset = BlockPos.max(
					BlockPos.min(
							NbtHelper.toBlockPos(nbt, "relay_block_position_offset").orElse(BlockPos.ORIGIN),
							new BlockPos(48, 48, 48)
					),
					new BlockPos(-48, -48, -48)
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

	// region --- getter & setter ---
	public BlockPos getRelayBlockPositionOffset() {
		return this.relayBlockPositionOffset;
	}

	public void setRelayBlockPositionOffset(BlockPos relayBlockPositionOffset) {
		this.relayBlockPositionOffset = new BlockPos(relayBlockPositionOffset);
	}
	// endregion --- getter & setter ---

	@Override
	protected void onRotate(BlockState state) {
		if (state.getBlock() instanceof RotatedBlockWithEntity) {
			if (state.get(ScriptBlocksBlockProperties.ROTATED) != this.rotated) {
				BlockRotation blockRotation = BlockRotationUtils.calculateRotationFromDifferentRotatedStates(state.get(ScriptBlocksBlockProperties.ROTATED), this.rotated);
				this.relayBlockPositionOffset = BlockRotationUtils.rotateOffsetBlockPos(this.relayBlockPositionOffset, blockRotation);
				this.rotated = state.get(ScriptBlocksBlockProperties.ROTATED);
			}
			if (state.get(ScriptBlocksBlockProperties.X_MIRRORED) != this.x_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.FRONT_BACK);
				this.x_mirrored = state.get(ScriptBlocksBlockProperties.X_MIRRORED);
			}
			if (state.get(ScriptBlocksBlockProperties.Z_MIRRORED) != this.z_mirrored) {
				this.relayBlockPositionOffset = BlockRotationUtils.mirrorOffsetBlockPos(this.relayBlockPositionOffset, BlockMirror.LEFT_RIGHT);
				this.z_mirrored = state.get(ScriptBlocksBlockProperties.Z_MIRRORED);
			}
		}
	}
}
