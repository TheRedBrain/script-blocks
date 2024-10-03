package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
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

public class DialogueAnswersRegistry {

	public static Map<Identifier, DialogueAnswer> registeredDialogueAnswers = new HashMap<>();
	private static final Type registeredDialogueAnswersFileFormat = new TypeToken<DialogueAnswer>() {
	}.getType();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return ScriptBlocks.identifier("dialogue_answer");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						registeredDialogueAnswers = new HashMap<>();
						for (var entry : resourceManager.findResources("dialogue_answer", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
							var identifier = entry.getKey();
							var resource = entry.getValue();
							try {
								JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
								DialogueAnswer dialogueAnswer = new Gson().fromJson(reader, registeredDialogueAnswersFileFormat);
								var id = identifier
										.toString().replace("dialogue_answer/", "");
								id = id.substring(0, id.lastIndexOf('.'));
								registeredDialogueAnswers.put(Identifier.of(id), dialogueAnswer);
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
