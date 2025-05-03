package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.spawner.EntityDetector;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.Optional;

public record LootableVaultConfig(
		String lootableIdentifier,
		double activationRange,
		double deactivationRange,
		ItemStack keyItem,
		Optional<RegistryKey<LootTable>> overrideLootTableToDisplay,
		EntityDetector playerDetector,
		EntityDetector.Selector entitySelector
) {
	static final String CONFIG_KEY = "config";
	public static LootableVaultConfig DEFAULT = new LootableVaultConfig();
	public static Codec<LootableVaultConfig> CODEC = RecordCodecBuilder.<LootableVaultConfig>create(
					instance -> instance.group(
									Codec.STRING.lenientOptionalFieldOf("lootable_identifier", DEFAULT.lootableIdentifier).forGetter(LootableVaultConfig::lootableIdentifier),
									Codec.DOUBLE.lenientOptionalFieldOf("activation_range", Double.valueOf(DEFAULT.activationRange())).forGetter(LootableVaultConfig::activationRange),
									Codec.DOUBLE.lenientOptionalFieldOf("deactivation_range", Double.valueOf(DEFAULT.deactivationRange())).forGetter(LootableVaultConfig::deactivationRange),
									ItemStack.createOptionalCodec("key_item").forGetter(LootableVaultConfig::keyItem),
									RegistryKey.createCodec(RegistryKeys.LOOT_TABLE)
											.lenientOptionalFieldOf("override_loot_table_to_display")
											.forGetter(LootableVaultConfig::overrideLootTableToDisplay)
							)
							.apply(instance, LootableVaultConfig::new)
			)
			.validate(LootableVaultConfig::validate);

	private LootableVaultConfig() {
		this(
				"",
				4.0,
				4.5,
				new ItemStack(Items.TRIAL_KEY),
				Optional.empty(),
				EntityDetector.NON_SPECTATOR_PLAYERS,
				EntityDetector.Selector.IN_WORLD
		);
	}

	public LootableVaultConfig(
			String lootableIdentifier,
			double activationRange,
			double deactivationRange,
			ItemStack keyItem,
			Optional<RegistryKey<LootTable>> overrideLootTableToDisplay
	) {
		this(lootableIdentifier, activationRange, deactivationRange, keyItem, overrideLootTableToDisplay, DEFAULT.playerDetector(), DEFAULT.entitySelector());
	}

	private DataResult<LootableVaultConfig> validate() {
		return this.activationRange > this.deactivationRange
				? DataResult.error(() -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")")
				: DataResult.success(this);
	}
}
