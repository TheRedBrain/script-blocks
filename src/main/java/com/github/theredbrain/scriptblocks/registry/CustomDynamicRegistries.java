package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.data.Boss;
import com.github.theredbrain.scriptblocks.data.Dialogue;
import com.github.theredbrain.scriptblocks.data.DialogueAnswer;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.data.LootableVaultConfig;
import com.github.theredbrain.scriptblocks.data.PVPArenaSettings;
import com.github.theredbrain.scriptblocks.data.Shop;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class CustomDynamicRegistries {

	public static final RegistryKey<Registry<Boss>> BOSS_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("bosses"));
	public static final RegistryKey<Registry<DialogueAnswer>> DIALOGUE_ANSWER_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("dialogue_answers"));
	public static final RegistryKey<Registry<Dialogue>> DIALOGUE_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("dialogues"));
	public static final RegistryKey<Registry<Location>> LOCATION_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("locations"));
	public static final RegistryKey<Registry<LootableVaultConfig>> LOOTABLE_VAULT_CONFIG_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("lootable_vault_configs"));
	public static final RegistryKey<Registry<PVPArenaSettings>> PVP_ARENA_SETTINGS_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("pvp_arena_settings"));
	public static final RegistryKey<Registry<Shop>> SHOP_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("shops"));

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
