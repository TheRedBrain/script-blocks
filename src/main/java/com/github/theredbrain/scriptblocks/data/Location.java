package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record Location(
		BlockPos controlBlockPos,
		String structureIdentifier,
		String displayName,
		boolean showLocationOwner,
		boolean isPublic,
		boolean allowRegeneration,
		Location.Availability availability,
		Map<String, SideEntrance> side_entrances
) {

	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.optionalFieldOf("control_block_pos", BlockPos.ORIGIN).forGetter(x -> x.controlBlockPos),
			Codec.STRING.optionalFieldOf("structure_identifier", "").forGetter(x -> x.structureIdentifier),
			Codec.STRING.optionalFieldOf("display_name", "").forGetter(x -> x.displayName),
			Codec.BOOL.optionalFieldOf("show_location_owner", true).forGetter(x -> x.showLocationOwner),
			Codec.BOOL.optionalFieldOf("is_public", true).forGetter(x -> x.isPublic),
			Codec.BOOL.optionalFieldOf("allow_regeneration", true).forGetter(x -> x.allowRegeneration),
			Location.Availability.CODEC.fieldOf("availability").forGetter(x -> x.availability),
			Codec.unboundedMap(Codec.STRING, SideEntrance.CODEC).optionalFieldOf("side_entrances", new HashMap<>()).forGetter(x -> x.side_entrances)
	).apply(instance, Location::new));

	public Location(
			BlockPos controlBlockPos,
			String structureIdentifier,
			String displayName,
			boolean showLocationOwner,
			boolean isPublic,
			boolean allowRegeneration,
			Location.Availability availability,
			Map<String, SideEntrance> side_entrances
	) {
		this.controlBlockPos = controlBlockPos != null ? controlBlockPos : BlockPos.ORIGIN;
		this.structureIdentifier = structureIdentifier != null ? structureIdentifier : "";
		this.displayName = displayName != null ? displayName : "";
		this.availability = availability;
		this.showLocationOwner = showLocationOwner;
		this.isPublic = isPublic;
		this.allowRegeneration = allowRegeneration;
		this.side_entrances = side_entrances != null ? side_entrances : new HashMap<>();
	}

	public record SideEntrance(
			String name,
			boolean showLocationOwner,
			boolean isEntranceNameShownFirst,
			boolean allowRegeneration,
			Location.Availability availability
	) {

		public static final Codec<SideEntrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("name", "").forGetter(x -> x.name),
				Codec.BOOL.optionalFieldOf("show_location_owner", true).forGetter(x -> x.showLocationOwner),
				Codec.BOOL.optionalFieldOf("is_entrance_name_shown_first", true).forGetter(x -> x.isEntranceNameShownFirst),
				Codec.BOOL.optionalFieldOf("allow_regeneration", true).forGetter(x -> x.allowRegeneration),
				Location.Availability.CODEC.fieldOf("availability").forGetter(x -> x.availability)
		).apply(instance, SideEntrance::new));

		public SideEntrance(
				String name,
				boolean showLocationOwner,
				boolean isEntranceNameShownFirst,
				boolean allowRegeneration,
				Location.Availability availability
		) {
			this.name = name != null ? name : "";
			this.showLocationOwner = showLocationOwner;
			this.isEntranceNameShownFirst = isEntranceNameShownFirst;
			this.allowRegeneration = allowRegeneration;
			this.availability = availability;
		}
	}

	public record Availability(
			Optional<LootContextPredicate> unlock_predicate,
			CommonDataStructures.DataCheck unlockDataCheck,
			CommonDataStructures.DataCheck lockDataCheck,
			boolean showLockedLocation,
			List<CommonDataStructures.ItemCost> itemCosts,
			boolean showUnaffordableLocation,
			long cooldown
	) {

		public static final Codec<Location.Availability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("unlock_predicate").forGetter(x -> x.unlock_predicate),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("unlock_data_check", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.unlockDataCheck),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("lock_data_check", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.lockDataCheck),
				Codec.BOOL.optionalFieldOf("show_locked_location", true).forGetter(x -> x.showLockedLocation),
				CommonDataStructures.ItemCost.CODEC.listOf().optionalFieldOf("item_costs", List.of()).forGetter(x -> x.itemCosts),
				Codec.BOOL.optionalFieldOf("show_unaffordable_location", true).forGetter(x -> x.showUnaffordableLocation),
				Codec.LONG.optionalFieldOf("cooldown", 0L).forGetter(x -> x.cooldown)
		).apply(instance, Location.Availability::new));

		public Availability(
				Optional<LootContextPredicate> unlock_predicate,
				CommonDataStructures.DataCheck unlockDataCheck,
				CommonDataStructures.DataCheck lockDataCheck,
				boolean showLockedLocation,
				List<CommonDataStructures.ItemCost> itemCosts,
				boolean showUnaffordableLocation,
				long cooldown
		) {
			this.unlock_predicate = unlock_predicate;
			this.unlockDataCheck = unlockDataCheck != null ? unlockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.lockDataCheck = lockDataCheck != null ? lockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.showLockedLocation = showLockedLocation;
			this.itemCosts = itemCosts != null ? itemCosts : List.of();
			this.showUnaffordableLocation = showUnaffordableLocation;
			this.cooldown = Math.max(0L, cooldown);
		}
	}

}
