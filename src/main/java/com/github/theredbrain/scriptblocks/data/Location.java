package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public record Location(
		BlockPos controlBlockPos,
		@Nullable Identifier structureIdentifier,
		String displayName,
		@Nullable Identifier unlockAdvancement,
		@Nullable Identifier lockAdvancement,
		boolean showLockedLocation,
		boolean showUnlockAdvancement,
		boolean showLockAdvancement,
		boolean showLocationName,
		boolean showLocationOwner,
		boolean isPublic,
		boolean canOwnerBeChosen,
		boolean consumeKey,
		@Nullable ItemStack key,
		@Nullable Map<String, SideEntrance> side_entrances
) {

	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.optionalFieldOf("controlBlockPos", BlockPos.ORIGIN).forGetter(x -> x.controlBlockPos),
			Identifier.CODEC.optionalFieldOf("structureIdentifier", null).forGetter(x -> x.structureIdentifier),
			Codec.STRING.optionalFieldOf("displayName", "").forGetter(x -> x.displayName),
			Identifier.CODEC.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
			Identifier.CODEC.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockedLocation", true).forGetter(x -> x.showLockedLocation),
			Codec.BOOL.optionalFieldOf("showUnlockAdvancement", true).forGetter(x -> x.showUnlockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockAdvancement", true).forGetter(x -> x.showLockAdvancement),
			Codec.BOOL.optionalFieldOf("showLocationName", true).forGetter(x -> x.showLocationName),
			Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner),
			Codec.BOOL.optionalFieldOf("isPublic", true).forGetter(x -> x.isPublic),
			Codec.BOOL.optionalFieldOf("canOwnerBeChosen", true).forGetter(x -> x.canOwnerBeChosen),
			Codec.BOOL.optionalFieldOf("consumeKey", true).forGetter(x -> x.consumeKey),
			ItemStack.CODEC.optionalFieldOf("key", ItemStack.EMPTY).forGetter(x -> x.key),
			Codec.unboundedMap(Codec.STRING, SideEntrance.CODEC).optionalFieldOf("side_entrances", new HashMap<>()).forGetter(x -> x.side_entrances)
	).apply(instance, Location::new));

	public Location(
			BlockPos controlBlockPos,
			@Nullable Identifier structureIdentifier,
			String displayName,
			@Nullable Identifier unlockAdvancement,
			@Nullable Identifier lockAdvancement,
			boolean showLockedLocation,
			boolean showUnlockAdvancement,
			boolean showLockAdvancement,
			boolean showLocationName,
			boolean showLocationOwner,
			boolean isPublic,
			boolean canOwnerBeChosen,
			boolean consumeKey,
			@Nullable ItemStack key,
			@Nullable Map<String, SideEntrance> side_entrances
	) {
		this.controlBlockPos = controlBlockPos;
		this.structureIdentifier = structureIdentifier;
		this.displayName = displayName;
		this.unlockAdvancement = unlockAdvancement;
		this.lockAdvancement = lockAdvancement;
		this.showLockedLocation = showLockedLocation;
		this.showUnlockAdvancement = showUnlockAdvancement;
		this.showLockAdvancement = showLockAdvancement;
		this.showLocationName = showLocationName;
		this.showLocationOwner = showLocationOwner;
		this.isPublic = isPublic;
		this.canOwnerBeChosen = canOwnerBeChosen;
		this.consumeKey = consumeKey;
		this.key = key;
		this.side_entrances = side_entrances;
	}

	public record SideEntrance(
			String name,
			@Nullable Identifier unlockAdvancement,
			@Nullable Identifier lockAdvancement,
			boolean showLockedLocation,
			boolean showUnlockAdvancement,
			boolean showLockAdvancement,
			boolean showLocationName,
			boolean showLocationOwner,
			boolean consumeKey,
			@Nullable ItemStack key
	) {

		public static final Codec<SideEntrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("name", "").forGetter(x -> x.name),
				Identifier.CODEC.optionalFieldOf("lockAdvancement", null).forGetter(x -> x.lockAdvancement),
				Identifier.CODEC.optionalFieldOf("unlockAdvancement", null).forGetter(x -> x.unlockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockedLocation", true).forGetter(x -> x.showLockedLocation),
				Codec.BOOL.optionalFieldOf("showUnlockAdvancement", true).forGetter(x -> x.showUnlockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockAdvancement", true).forGetter(x -> x.showLockAdvancement),
				Codec.BOOL.optionalFieldOf("showLocationName", true).forGetter(x -> x.showLocationName),
				Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner),
				Codec.BOOL.optionalFieldOf("consumeKey", true).forGetter(x -> x.consumeKey),
				ItemStack.CODEC.optionalFieldOf("key", ItemStack.EMPTY).forGetter(x -> x.key)
		).apply(instance, SideEntrance::new));

		public SideEntrance(
				String name,
				@Nullable Identifier unlockAdvancement,
				@Nullable Identifier lockAdvancement,
				boolean showLockedLocation,
				boolean showUnlockAdvancement,
				boolean showLockAdvancement,
				boolean showLocationName,
				boolean showLocationOwner,
				boolean consumeKey,
				@Nullable ItemStack key
		) {
			this.name = name;
			this.unlockAdvancement = unlockAdvancement;
			this.lockAdvancement = lockAdvancement;
			this.showLockedLocation = showLockedLocation;
			this.showUnlockAdvancement = showUnlockAdvancement;
			this.showLockAdvancement = showLockAdvancement;
			this.showLocationName = showLocationName;
			this.showLocationOwner = showLocationOwner;
			this.consumeKey = consumeKey;
			this.key = key;
		}
	}
}
