package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Dialogue;
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

public class DialoguesRegistry {

	public static Map<Identifier, Dialogue> registeredDialogues = new HashMap<>();
	private static final Type registeredDialoguesFileFormat = new TypeToken<Dialogue>() {
	}.getType();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return ScriptBlocks.identifier("dialogues");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						registeredDialogues = new HashMap<>();
						for (var entry : resourceManager.findResources("dialogues", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
							var identifier = entry.getKey();
							var resource = entry.getValue();
							try {
								JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
								Dialogue dialogue = new Gson().fromJson(reader, registeredDialoguesFileFormat);
								var id = identifier
										.toString().replace("dialogues/", "");
								id = id.substring(0, id.lastIndexOf('.'));
								registeredDialogues.put(Identifier.of(id), dialogue);
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
