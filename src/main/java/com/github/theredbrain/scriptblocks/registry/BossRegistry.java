package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Boss;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BossRegistry {

	public static Map<Identifier, Boss> registeredBosses = new HashMap<>();
	private static final Type registeredBossesFileFormat = new TypeToken<Boss>() {
	}.getType();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return ScriptBlocks.identifier("bosses");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						registeredBosses = new HashMap<>();
						for (var entry : resourceManager.findResources("bosses", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
							var identifier = entry.getKey();
							var resource = entry.getValue();
							try {
								JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
								Boss boss = new Gson().fromJson(reader, registeredBossesFileFormat);
								var id = identifier
										.toString().replace("bosses/", "");
								id = id.substring(0, id.lastIndexOf('.'));
								registeredBosses.put(Identifier.of(id), boss);
							} catch (Exception e) {
								System.err.println("Failed to parse: " + identifier);
								e.printStackTrace();
							}
						}
					}
				}
		);
	}
}
