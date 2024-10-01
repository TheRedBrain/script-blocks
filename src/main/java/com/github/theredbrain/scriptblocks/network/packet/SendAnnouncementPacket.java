package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record SendAnnouncementPacket(Text announcement) implements CustomPayload {
	public static final CustomPayload.Id<SendAnnouncementPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("send_announcement"));
	public static final PacketCodec<RegistryByteBuf, SendAnnouncementPacket> PACKET_CODEC = PacketCodec.of(SendAnnouncementPacket::write, SendAnnouncementPacket::new);

	public SendAnnouncementPacket(RegistryByteBuf registryByteBuf) {
		this(TextCodecs.PACKET_CODEC.decode(registryByteBuf));
	}

	private void write(RegistryByteBuf registryByteBuf) {
		TextCodecs.PACKET_CODEC.encode(registryByteBuf, this.announcement);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
