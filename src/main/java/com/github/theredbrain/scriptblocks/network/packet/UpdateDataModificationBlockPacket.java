package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.DataModificationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateDataModificationBlockPacket(
		BlockPos dataAccessBlockPosition,
		BlockPos dataProvidingBlockPositionOffset,
		DataModificationBlockEntity.DataModificationMode dataModificationMode,
		int addedIntegerValue,
		int integerModulusValue
) implements CustomPacketPayload {
	public static final Type<UpdateDataModificationBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_data_modification_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDataModificationBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDataModificationBlockPacket::write, UpdateDataModificationBlockPacket::new);

	public UpdateDataModificationBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readEnum(DataModificationBlockEntity.DataModificationMode.class),
				registryByteBuf.readInt(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dataAccessBlockPosition);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPositionOffset);
		registryByteBuf.writeEnum(this.dataModificationMode);
		registryByteBuf.writeInt(this.addedIntegerValue);
		registryByteBuf.writeInt(this.integerModulusValue);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
