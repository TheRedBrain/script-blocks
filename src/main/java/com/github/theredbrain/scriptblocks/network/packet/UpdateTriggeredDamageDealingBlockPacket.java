package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeredDamageDealingBlockPacket(
		BlockPos triggeredDamageDealingBlockPosition,
		String selectionMode,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset,
		BlockPos uuidListHandlerPositionOffset,
		String exceptionTagIdentifierString,
		String damageTypeIdentifierString,
		float damageAmount
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredDamageDealingBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_damage_dealing_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredDamageDealingBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredDamageDealingBlockPacket::write, UpdateTriggeredDamageDealingBlockPacket::new);

	public UpdateTriggeredDamageDealingBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				new Vec3i(
						registryByteBuf.readInt(),
						registryByteBuf.readInt(),
						registryByteBuf.readInt()
				),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readFloat()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredDamageDealingBlockPosition);
		registryByteBuf.writeUtf(this.selectionMode);
		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
		registryByteBuf.writeBlockPos(this.uuidListHandlerPositionOffset);
		registryByteBuf.writeUtf(this.exceptionTagIdentifierString);
		registryByteBuf.writeUtf(this.damageTypeIdentifierString);
		registryByteBuf.writeFloat(this.damageAmount);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
