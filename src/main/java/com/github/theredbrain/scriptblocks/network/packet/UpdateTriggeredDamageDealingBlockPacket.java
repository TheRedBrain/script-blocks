package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record UpdateTriggeredDamageDealingBlockPacket(
		BlockPos triggeredDamageDealingBlockPosition,
		boolean showArea,
		Vec3i areaDimensions,
		BlockPos areaPositionOffset
) implements CustomPayload {
	public static final Id<UpdateTriggeredDamageDealingBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggered_damage_dealing_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredDamageDealingBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredDamageDealingBlockPacket::write, UpdateTriggeredDamageDealingBlockPacket::new);

	public UpdateTriggeredDamageDealingBlockPacket(RegistryByteBuf registryByteBuf) {
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

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredDamageDealingBlockPosition);

		registryByteBuf.writeBoolean(this.showArea);
		registryByteBuf.writeInt(this.areaDimensions.getX());
		registryByteBuf.writeInt(this.areaDimensions.getY());
		registryByteBuf.writeInt(this.areaDimensions.getZ());
		registryByteBuf.writeBlockPos(this.areaPositionOffset);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
