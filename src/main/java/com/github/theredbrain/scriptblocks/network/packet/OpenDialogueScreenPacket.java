package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record OpenDialogueScreenPacket(BlockPos dialogueBlockPos,
									   String responseDialogueIdentifier) implements CustomPayload {
	public static final CustomPayload.Id<OpenDialogueScreenPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("open_dialogue_screen"));
	public static final PacketCodec<RegistryByteBuf, OpenDialogueScreenPacket> PACKET_CODEC = PacketCodec.of(OpenDialogueScreenPacket::write, OpenDialogueScreenPacket::new);

	public OpenDialogueScreenPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readString());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dialogueBlockPos);
		registryByteBuf.writeString(this.responseDialogueIdentifier);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
