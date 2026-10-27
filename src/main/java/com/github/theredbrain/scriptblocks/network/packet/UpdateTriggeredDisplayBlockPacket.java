package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record UpdateTriggeredDisplayBlockPacket(
		BlockPos triggeredDisplayBlockPosition,
		String billboardModeString,
		String displayModeString,
		boolean isTriggered,
		Vec3 displayOffset,
		float displayYaw,
		float displayPitch,
		String displayTextString,
		BlockPos dataProvidingBlockPosOffset,
		String dataIdentifierString,
		int lineWidth,
		Byte textOpacity,
		int textBackground
) implements CustomPacketPayload {
	public static final Type<UpdateTriggeredDisplayBlockPacket> PACKET_ID = new Type<>(ScriptBlocks.identifier("update_triggered_display_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredDisplayBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredDisplayBlockPacket::write, UpdateTriggeredDisplayBlockPacket::new);

	public UpdateTriggeredDisplayBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readVec3(),
				registryByteBuf.readFloat(),
				registryByteBuf.readFloat(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readByte(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredDisplayBlockPosition);
		registryByteBuf.writeUtf(this.billboardModeString);
		registryByteBuf.writeUtf(this.displayModeString);
		registryByteBuf.writeBoolean(this.isTriggered);
		registryByteBuf.writeVec3(this.displayOffset);
		registryByteBuf.writeFloat(this.displayYaw);
		registryByteBuf.writeFloat(this.displayPitch);
		registryByteBuf.writeUtf(this.displayTextString);
		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);
		registryByteBuf.writeUtf(this.dataIdentifierString);
		registryByteBuf.writeInt(this.lineWidth);
		registryByteBuf.writeByte(this.textOpacity);
		registryByteBuf.writeInt(this.textBackground);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
