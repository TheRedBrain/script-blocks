package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public record UpdateTriggeredDisplayBlockPacket(
		BlockPos triggeredDisplayBlockPosition,
		String billboardModeString,
		String displayModeString,
		boolean isTriggered,
		Vec3d displayOffset,
		String displayTextString,
		int lineWidth,
		Byte textOpacity,
		int textBackground
) implements CustomPayload {
	public static final Id<UpdateTriggeredDisplayBlockPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("update_triggered_display_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredDisplayBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredDisplayBlockPacket::write, UpdateTriggeredDisplayBlockPacket::new);

	public UpdateTriggeredDisplayBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readVec3d(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readByte(),
				registryByteBuf.readInt()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredDisplayBlockPosition);
		registryByteBuf.writeString(this.billboardModeString);
		registryByteBuf.writeString(this.displayModeString);
		registryByteBuf.writeBoolean(this.isTriggered);
		registryByteBuf.writeVec3d(this.displayOffset);
		registryByteBuf.writeString(this.displayTextString);
		registryByteBuf.writeInt(this.lineWidth);
		registryByteBuf.writeByte(this.textOpacity);
		registryByteBuf.writeInt(this.textBackground);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
