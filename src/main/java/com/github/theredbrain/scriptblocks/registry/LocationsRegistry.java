package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Location;
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

public class LocationsRegistry {

	public static Map<Identifier, Location> registeredLocations = new HashMap<>();
	private static final Type registeredLocationsFileFormat = new TypeToken<Location>() {
	}.getType();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return ScriptBlocks.identifier("locations");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						registeredLocations = new HashMap<>();
						for (var entry : resourceManager.findResources("locations", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
							var identifier = entry.getKey();
							var resource = entry.getValue();
							try {
								JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
								Location location = new Gson().fromJson(reader, registeredLocationsFileFormat);
								var id = identifier
										.toString().replace("locations/", "");
								id = id.substring(0, id.lastIndexOf('.'));
								registeredLocations.put(Identifier.of(id), location);
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
