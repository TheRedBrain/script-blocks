package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.Optional;

public record LootableVaultConfig(
		String lootableIdentifier,
		int rolls,
		int choices,
		boolean withChoice,
		double activationRange,
		double deactivationRange,
		ItemStack keyItem,
		Optional<RegistryKey<LootTable>> overrideLootTableToDisplay/*,
		EntityDetector playerDetector,
		EntityDetector.Selector entitySelector*/
) {
//	static final String CONFIG_KEY = "config";
	public static LootableVaultConfig DEFAULT = new LootableVaultConfig();
	public static final Codec<LootableVaultConfig> CODEC = RecordCodecBuilder.<LootableVaultConfig>create(
					instance -> instance.group(
									Codec.STRING.fieldOf("lootableIdentifier").forGetter(LootableVaultConfig::lootableIdentifier),
									Codec.INT.fieldOf("rolls").forGetter(LootableVaultConfig::rolls),
									Codec.INT.fieldOf("choices").forGetter(LootableVaultConfig::choices),
									Codec.BOOL.fieldOf("withChoice").forGetter(LootableVaultConfig::withChoice),
									Codec.DOUBLE.fieldOf("activationRange").forGetter(LootableVaultConfig::activationRange),
									Codec.DOUBLE.fieldOf("deactivationRange").forGetter(LootableVaultConfig::deactivationRange),
									ItemStack.VALIDATED_CODEC.fieldOf("keyItem").forGetter(LootableVaultConfig::keyItem),
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
				3,
				1,
				true,
				4.0,
				4.5,
				new ItemStack(Items.TRIAL_KEY),
				Optional.empty()//,
//				EntityDetector.NON_SPECTATOR_PLAYERS,
//				EntityDetector.Selector.IN_WORLD
		);
	}

//	public LootableVaultConfig(
//			String lootableIdentifier,
//			int rolls,
//			int choices,
//			boolean withChoice,
//			double activationRange,
//			double deactivationRange/*,
//			ItemStack keyItem,
//			Optional<RegistryKey<LootTable>> overrideLootTableToDisplay*/
//	) {
//		this(
//				lootableIdentifier,
//				rolls,
//				choices,
//				withChoice,
//				activationRange,
//				deactivationRange//,
////				ItemStack.EMPTY,
////				overrideLootTableToDisplay,
////				DEFAULT.playerDetector(),
////				DEFAULT.entitySelector()
//		);
//	}

	private DataResult<LootableVaultConfig> validate() {
		return this.activationRange > this.deactivationRange
				? DataResult.error(() -> "Activation range must (" + this.activationRange + ") be less or equal to deactivation range (" + this.deactivationRange + ")")
				: DataResult.success(this);
	}
}
