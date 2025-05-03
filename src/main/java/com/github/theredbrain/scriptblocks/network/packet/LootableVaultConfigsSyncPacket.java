package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record LootableVaultConfigsSyncPacket(
		Map<Identifier, LootableVaultConfig> registeredLootableVaultConfigs) implements CustomPayload {
	public static final Id<LootableVaultConfigsSyncPacket> PACKET_ID = new Id<>(ScriptBlocks.identifier("lootable_vault_configs_sync"));
	public static final PacketCodec<RegistryByteBuf, LootableVaultConfigsSyncPacket> PACKET_CODEC = PacketCodec.of(LootableVaultConfigsSyncPacket::write, LootableVaultConfigsSyncPacket::read);

	public static LootableVaultConfigsSyncPacket read(RegistryByteBuf registryByteBuf) {
		Map<Identifier, LootableVaultConfig> newLootableVaultConfigs = new HashMap<>();
		int i = registryByteBuf.readInt();
		for (int j = 0; j < i; j++) {
			Identifier identifier = registryByteBuf.readIdentifier();
			LootableVaultConfig lootableVaultConfig = registryByteBuf.decodeAsJson(LootableVaultConfig.CODEC);
			newLootableVaultConfigs.put(identifier, lootableVaultConfig);
		}
		return new LootableVaultConfigsSyncPacket(newLootableVaultConfigs);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeInt(registeredLootableVaultConfigs.size());
		for (var entry : registeredLootableVaultConfigs.entrySet()) {
			registryByteBuf.writeIdentifier(entry.getKey());
			registryByteBuf.encodeAsJson(LootableVaultConfig.CODEC, entry.getValue());
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}