package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateTriggeredAdvancementCheckerBlockPacket(
		BlockPos triggeredAdvancementCheckerBlockPosition,
		BlockPos firstTriggeredBlockPositionOffset,
		boolean firstTriggeredBlockResets,
		BlockPos secondTriggeredBlockPositionOffset,
		boolean secondTriggeredBlockResets,
		String checkedAdvancementIdentifier
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateTriggeredAdvancementCheckerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_triggered_advancement_checker_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTriggeredAdvancementCheckerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTriggeredAdvancementCheckerBlockPacket::write, UpdateTriggeredAdvancementCheckerBlockPacket::new);

	public UpdateTriggeredAdvancementCheckerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.triggeredAdvancementCheckerBlockPosition);
		registryByteBuf.writeBlockPos(this.firstTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.firstTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.secondTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.secondTriggeredBlockResets);
		registryByteBuf.writeUtf(this.checkedAdvancementIdentifier);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
