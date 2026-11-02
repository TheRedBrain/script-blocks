package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TriggeredDisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

public record UpdateTriggeredDisplayBlockPacket(
		BlockPos triggeredDisplayBlockPosition,
		TriggeredDisplayBlockEntity.BillboardConstraints billboardConstraint,
		TriggeredDisplayBlockEntity.DisplayMode displayMode,
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
				registryByteBuf.readEnum(TriggeredDisplayBlockEntity.BillboardConstraints.class),
				registryByteBuf.readEnum(TriggeredDisplayBlockEntity.DisplayMode.class),
				registryByteBuf.readBoolean(),
				Vec3.STREAM_CODEC.decode(registryByteBuf),
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
		registryByteBuf.writeEnum(this.billboardConstraint);
		registryByteBuf.writeEnum(this.displayMode);
		registryByteBuf.writeBoolean(this.isTriggered);
		Vec3.STREAM_CODEC.encode(registryByteBuf, this.displayOffset);
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
