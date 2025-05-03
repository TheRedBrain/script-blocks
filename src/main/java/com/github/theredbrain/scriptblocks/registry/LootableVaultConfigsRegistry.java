package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.util.DebuggingHelper;
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

public class LootableVaultConfigsRegistry {

	public static Map<Identifier, LootableVaultConfig> registeredLootableVaultConfigs = new HashMap<>();
	private static final Type registeredLootableVaultConfigsFileFormat = new TypeToken<LootableVaultConfig>() {
	}.getType();

	public static void init() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(
				new SimpleSynchronousResourceReloadListener() {
					@Override
					public Identifier getFabricId() {
						return ScriptBlocks.identifier("lootable_vault_configs");
					}

					@Override
					public void reload(ResourceManager resourceManager) {
						registeredLootableVaultConfigs = new HashMap<>();
						for (var entry : resourceManager.findResources("lootable_vault_configs", fileName -> fileName.getPath().endsWith(".json")).entrySet()) {
							var identifier = entry.getKey();
							var resource = entry.getValue();
							try {
								JsonReader reader = new JsonReader(new InputStreamReader(resource.getInputStream()));
								LootableVaultConfig lootableVaultConfig = new Gson().fromJson(reader, registeredLootableVaultConfigsFileFormat);
								var id = identifier
										.toString().replace("lootable_vault_configs/", "");
								id = id.substring(0, id.lastIndexOf('.'));
								if (DebuggingHelper.isRegistryLoggingEnabled()) {
									DebuggingHelper.sendDebuggingMessage("Registered LootableVaultConfig: " + lootableVaultConfig, null);
								}
								registeredLootableVaultConfigs.put(Identifier.of(id), lootableVaultConfig);
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
