package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeredEntityRemoverBlockPacket(
		BlockPos triggeredEntityRemoverBlockPosition,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredEntityRemoverBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_entity_remover_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredEntityRemoverBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredEntityRemoverBlockPacket::write, UpdateTriggeredEntityRemoverBlockPacket::new);

	public UpdateTriggeredEntityRemoverBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredEntityRemoverBlockPosition);

		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
