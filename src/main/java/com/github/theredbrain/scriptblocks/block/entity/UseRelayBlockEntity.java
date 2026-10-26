package com.github.theredbrain.scriptblocks.block.entity;

import com.github.theredbrain.scriptblocks.block.RotatedBlockWithEntity;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksEntities;
import com.github.theredbrain.scriptblocks.util.BlockRotationUtils;
import com.github.theredbrain.scriptblocks.util.ScriptBlocksBlockProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public class UseRelayBlockEntity extends RotatedBlockEntity {
	protected BlockPos relayBlockPositionOffset = BlockPos.ORIGIN;

	public UseRelayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public UseRelayBlockEntity(BlockPos pos, BlockState state) {
		this(ScriptBlocksEntities.USE_RELAY_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void writeData(WriteView view) {

		view.put("relay_block_position_offset", BlockPos.CODEC, this.relayBlockPositionOffset);

		super.writeData(view);
	}

	@Override
	protected void readData(ReadView view) {

		this.relayBlockPositionOffset = BlockPos.max(
				BlockPos.min(
						view.read("relay_block_position_offset", BlockPos.CODEC).orElse(BlockPos.ORIGIN),
						new BlockPos(48, 48, 48)
				),
				new BlockPos(-48, -48, -48)
		);

		super.readData(view);
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
