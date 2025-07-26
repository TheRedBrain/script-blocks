package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Location(
		BlockPos controlBlockPos,
		String structureIdentifier,
		String displayName,
		Location.Availability availability,
		boolean showLocationOwner,
		boolean isPublic,
		Map<String, SideEntrance> side_entrances
) {

	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.optionalFieldOf("controlBlockPos", BlockPos.ORIGIN).forGetter(x -> x.controlBlockPos),
			Codec.STRING.optionalFieldOf("structureIdentifier", "").forGetter(x -> x.structureIdentifier),
			Codec.STRING.optionalFieldOf("displayName", "").forGetter(x -> x.displayName),
			Location.Availability.CODEC.fieldOf("availability").forGetter(x -> x.availability),
			Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner),
			Codec.BOOL.optionalFieldOf("isPublic", true).forGetter(x -> x.isPublic),
			Codec.unboundedMap(Codec.STRING, SideEntrance.CODEC).optionalFieldOf("side_entrances", new HashMap<>()).forGetter(x -> x.side_entrances)
	).apply(instance, Location::new));

	public Location(
			BlockPos controlBlockPos,
			String structureIdentifier,
			String displayName,
			Location.Availability availability,
			boolean showLocationOwner,
			boolean isPublic,
			Map<String, SideEntrance> side_entrances
	) {
		this.controlBlockPos = controlBlockPos != null ? controlBlockPos : BlockPos.ORIGIN;
		this.structureIdentifier = structureIdentifier != null ? structureIdentifier : "";
		this.displayName = displayName != null ? displayName : "";
		this.availability = availability;
		this.showLocationOwner = showLocationOwner;
		this.isPublic = isPublic;
		this.side_entrances = side_entrances != null ? side_entrances : new HashMap<>();
	}

	public record SideEntrance(
			String name,
			Location.Availability availability,
			boolean showLocationOwner
	) {

		public static final Codec<SideEntrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("name", "").forGetter(x -> x.name),
				Location.Availability.CODEC.fieldOf("availability").forGetter(x -> x.availability),
				Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner)
		).apply(instance, SideEntrance::new));

		public SideEntrance(
				String name,
				Location.Availability availability,
				boolean showLocationOwner
		) {
			this.name = name != null ? name : "";
			this.availability = availability;
			this.showLocationOwner = showLocationOwner;
		}
	}

	public record Availability(
			String unlockAdvancement,
			String lockAdvancement,
			CommonDataStructures.DataCheck unlockDataCheck,
			CommonDataStructures.DataCheck lockDataCheck,
			boolean showLockedLocation,
			List<CommonDataStructures.ItemCost> itemCosts,
			boolean showUnaffordableLocation
	) {

		public static final Codec<Location.Availability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.STRING.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("unlockDataCheck", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.unlockDataCheck),
				CommonDataStructures.DataCheck.CODEC.optionalFieldOf("lockDataCheck", CommonDataStructures.DataCheck.DEFAULT).forGetter(x -> x.lockDataCheck),
				Codec.BOOL.optionalFieldOf("showLockedLocation", true).forGetter(x -> x.showLockedLocation),
				CommonDataStructures.ItemCost.CODEC.listOf().optionalFieldOf("itemCosts", List.of()).forGetter(x -> x.itemCosts),
				Codec.BOOL.optionalFieldOf("showUnaffordableLocation", true).forGetter(x -> x.showUnaffordableLocation)
		).apply(instance, Location.Availability::new));

		public Availability(
				String unlockAdvancement,
				String lockAdvancement,
				CommonDataStructures.DataCheck unlockDataCheck,
				CommonDataStructures.DataCheck lockDataCheck,
				boolean showLockedLocation,
				List<CommonDataStructures.ItemCost> itemCosts,
				boolean showUnaffordableLocation
		) {
			this.unlockAdvancement = unlockAdvancement != null ? unlockAdvancement : "";
			this.lockAdvancement = lockAdvancement != null ? lockAdvancement : "";
			this.unlockDataCheck = unlockDataCheck != null ? unlockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.lockDataCheck = lockDataCheck != null ? lockDataCheck : CommonDataStructures.DataCheck.DEFAULT;
			this.showLockedLocation = showLockedLocation;
			this.itemCosts = itemCosts != null ? itemCosts : List.of();
			this.showUnaffordableLocation = showUnaffordableLocation;
		}
	}

}
