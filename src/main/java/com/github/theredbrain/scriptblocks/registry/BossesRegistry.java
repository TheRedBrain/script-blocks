package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.data.BossHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BossesRegistry {

	static Map<Identifier, Boss> registeredBosses = new HashMap<>();

	public static void register(Identifier bossId, Boss boss) {
		registeredBosses.put(bossId, boss);
	}

	public static Boss getBoss(Identifier bossId) {
		return registeredBosses.get(bossId);
	}

	public static void init() {
		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer) -> {
			loadBosses(minecraftServer.getResourceManager());
			encodeRegistry();
		});
	}

	private static void loadBosses(ResourceManager resourceManager) {
		var gson = new Gson();
		Map<Identifier, Boss> registeredBosses = new HashMap();
		// Reading all attribute files
		for (var entry : resourceManager.findResources("bosses", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
			var identifier = entry.getKey();
			var resource = entry.getValue();
			try {
				// System.out.println("Checking resource: " + identifier);
				JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
				Boss boss = BossHelper.decode(reader);
				var id = identifier
						.toString().replace("bosses/", "");
				id = id.substring(0, id.lastIndexOf('.'));
				registeredBosses.put(new Identifier(id), boss);
			} catch (Exception e) {
				System.err.println("Failed to parse: " + identifier);
				e.printStackTrace();
			}
		}
		BossesRegistry.registeredBosses = registeredBosses;
	}

	// NETWORK SYNC

	private static PacketByteBuf encodedRegisteredBosses = PacketByteBufs.create();

	public static void encodeRegistry() {
		PacketByteBuf buffer = PacketByteBufs.create();
		var gson = new Gson();
		var json = gson.toJson(registeredBosses);
		if (ScriptBlocks.serverConfig.show_debug_log) {
			ScriptBlocks.LOGGER.info("Bosses registry loaded: " + json);
		}

		List<String> chunks = new ArrayList<>();
		var chunkSize = 10000;
		for (int i = 0; i < json.length(); i += chunkSize) {
			chunks.add(json.substring(i, Math.min(json.length(), i + chunkSize)));
		}

		buffer.writeInt(chunks.size());
		for (var chunk : chunks) {
			buffer.writeString(chunk);
		}

		if (ScriptBlocks.serverConfig.show_debug_log) {
			ScriptBlocks.LOGGER.info("Encoded Bosses registry size (with package overhead): " + buffer.readableBytes()
					+ " bytes (in " + chunks.size() + " string chunks with the size of " + chunkSize + ")");
		}
		encodedRegisteredBosses = buffer;
	}

	public static void decodeRegistry(PacketByteBuf buffer) {
		var chunkCount = buffer.readInt();
		String json = "";
		for (int i = 0; i < chunkCount; ++i) {
			json = json.concat(buffer.readString());
		}
		if (ScriptBlocks.serverConfig.show_debug_log) {
			ScriptBlocks.LOGGER.info("Decoded Bosses registry in " + chunkCount + " string chunks");
			ScriptBlocks.LOGGER.info("Bosses registry received: " + json);
		}
		var gson = new Gson();
		Type mapType = new TypeToken<Map<String, Boss>>() {
		}.getType();
		Map<String, Boss> readRegisteredBosses = gson.fromJson(json, mapType);
		Map<Identifier, Boss> newRegisteredBosses = new HashMap();
		readRegisteredBosses.forEach((key, value) -> {
			newRegisteredBosses.put(new Identifier(key), value);
		});
		registeredBosses = newRegisteredBosses;
	}

	public static PacketByteBuf getEncodedRegistry() {
		return encodedRegisteredBosses;
	}
}
