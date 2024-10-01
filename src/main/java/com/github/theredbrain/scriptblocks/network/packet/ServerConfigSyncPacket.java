package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.config.ServerConfig;
import com.google.gson.Gson;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ServerConfigSyncPacket(ServerConfig serverConfig) implements CustomPayload {
	public static final Id<ServerConfigSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("server_config_sync"));
	public static final PacketCodec<RegistryByteBuf, ServerConfigSyncPacket> PACKET_CODEC = PacketCodec.of(ServerConfigSyncPacket::write, ServerConfigSyncPacket::new);

	public ServerConfigSyncPacket(RegistryByteBuf registryByteBuf) {
		this(new Gson().fromJson(registryByteBuf.readString(), ServerConfig.class));
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeString(new Gson().toJson(serverConfig));
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
