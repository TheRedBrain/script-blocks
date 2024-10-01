package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateTriggeredAdvancementCheckerBlockPacket(BlockPos triggeredAdvancementCheckerBlockPosition,
														   BlockPos firstTriggeredBlockPositionOffset,
														   boolean firstTriggeredBlockResets,
														   BlockPos secondTriggeredBlockPositionOffset,
														   boolean secondTriggeredBlockResets,
														   String checkedAdvancementIdentifier) implements CustomPayload {
	public static final CustomPayload.Id<UpdateTriggeredAdvancementCheckerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_triggered_advancement_checker_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateTriggeredAdvancementCheckerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateTriggeredAdvancementCheckerBlockPacket::write, UpdateTriggeredAdvancementCheckerBlockPacket::new);

	public UpdateTriggeredAdvancementCheckerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readBlockPos(), registryByteBuf.readBoolean(), registryByteBuf.readBlockPos(), registryByteBuf.readBoolean(), registryByteBuf.readString());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredAdvancementCheckerBlockPosition);
		registryByteBuf.writeBlockPos(this.firstTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.firstTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.secondTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.secondTriggeredBlockResets);
		registryByteBuf.writeString(this.checkedAdvancementIdentifier);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
