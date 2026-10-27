package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.data.PVPArenaSettings;
import com.github.theredbrain.scriptblocks.data.Shop;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class ScriptBlocksDynamicRegistries {

	public static final ResourceKey<Registry<Boss>> BOSS_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("bosses"));
	public static final ResourceKey<Registry<DialogueAnswer>> DIALOGUE_ANSWER_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dialogue_answers"));
	public static final ResourceKey<Registry<Dialogue>> DIALOGUE_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dialogues"));
	public static final ResourceKey<Registry<Location>> LOCATION_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("locations"));
	@Deprecated
	public static final ResourceKey<Registry<LootableVaultConfig>> LOOTABLE_VAULT_CONFIG_REGISTRY_KEY = ResourceKey.createRegistryKey(ScriptBlocks.identifier("lootable_vault_configs"));
	public static final ResourceKey<Registry<PVPArenaSettings>> PVP_ARENA_SETTINGS_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("pvp_arena_settings"));
	public static final ResourceKey<Registry<Shop>> SHOP_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("shops"));

	public static void init() {
		DynamicRegistries.registerSynced(BOSS_REGISTRY_KEY, Boss.CODEC);
		DynamicRegistries.registerSynced(DIALOGUE_ANSWER_REGISTRY_KEY, DialogueAnswer.CODEC);
		DynamicRegistries.registerSynced(DIALOGUE_REGISTRY_KEY, Dialogue.CODEC);
		DynamicRegistries.registerSynced(LOCATION_REGISTRY_KEY, Location.CODEC);
		DynamicRegistries.registerSynced(LOOTABLE_VAULT_CONFIG_REGISTRY_KEY, LootableVaultConfig.CODEC);
		DynamicRegistries.registerSynced(PVP_ARENA_SETTINGS_REGISTRY_KEY, PVPArenaSettings.CODEC);
		DynamicRegistries.registerSynced(SHOP_REGISTRY_KEY, Shop.CODEC);
	}
}
