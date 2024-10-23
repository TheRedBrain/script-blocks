package com.github.theredbrain.scriptblocks.data;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

public record Location(
//		BlockPos controlBlockPos,
		int controlBlockPosX,
		int controlBlockPosY,
		int controlBlockPosZ,
		String structureIdentifier,
		String displayName,
		String unlockAdvancement,
		String lockAdvancement,
		boolean showLockedLocation,
		boolean showUnlockAdvancement,
		boolean showLockAdvancement,
//		boolean showLocationName, // TODO replace with check for empty displayName
		boolean showLocationOwner,
		boolean isPublic,
//		boolean canOwnerBeChosen, // TODO set in TeleporterBlock
		boolean consumeKey,
		String keyItemIdentifier,
		int keyItemCount,
		Map<String, SideEntrance> side_entrances
) {

	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("controlBlockPosX", 0).forGetter(x -> x.controlBlockPosX),
			Codec.INT.optionalFieldOf("controlBlockPosY", 0).forGetter(x -> x.controlBlockPosY),
			Codec.INT.optionalFieldOf("controlBlockPosZ", 0).forGetter(x -> x.controlBlockPosZ),
			Codec.STRING.optionalFieldOf("structureIdentifier", "").forGetter(x -> x.structureIdentifier),
			Codec.STRING.optionalFieldOf("displayName", "").forGetter(x -> x.displayName),
			Codec.STRING.optionalFieldOf("lockAdvancement", "").forGetter(x -> x.lockAdvancement),
			Codec.STRING.optionalFieldOf("unlockAdvancement", "").forGetter(x -> x.unlockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockedLocation", true).forGetter(x -> x.showLockedLocation),
			Codec.BOOL.optionalFieldOf("showUnlockAdvancement", true).forGetter(x -> x.showUnlockAdvancement),
			Codec.BOOL.optionalFieldOf("showLockAdvancement", true).forGetter(x -> x.showLockAdvancement),
//			Codec.BOOL.optionalFieldOf("showLocationName", true).forGetter(x -> x.showLocationName),
			Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner),
			Codec.BOOL.optionalFieldOf("isPublic", true).forGetter(x -> x.isPublic),
//			Codec.BOOL.optionalFieldOf("canOwnerBeChosen", true).forGetter(x -> x.canOwnerBeChosen),
			Codec.BOOL.optionalFieldOf("consumeKey", true).forGetter(x -> x.consumeKey),
			Codec.STRING.optionalFieldOf("keyItemIdentifier", "").forGetter(x -> x.keyItemIdentifier),
			Codec.INT.optionalFieldOf("keyItemCount", 0).forGetter(x -> x.keyItemCount),
			Codec.unboundedMap(Codec.STRING, SideEntrance.CODEC).optionalFieldOf("side_entrances", new HashMap<>()).forGetter(x -> x.side_entrances)
	).apply(instance, Location::new));

	public Location(
			int controlBlockPosX,
			int controlBlockPosY,
			int controlBlockPosZ,
			String structureIdentifier,
			String displayName,
			String unlockAdvancement,
			String lockAdvancement,
			boolean showLockedLocation,
			boolean showUnlockAdvancement,
			boolean showLockAdvancement,
//			boolean showLocationName,
			boolean showLocationOwner,
			boolean isPublic,
//			boolean canOwnerBeChosen,
			boolean consumeKey,
			String keyItemIdentifier,
			int keyItemCount,
			Map<String, SideEntrance> side_entrances
	) {
		this.controlBlockPosX = controlBlockPosX;
		this.controlBlockPosY = controlBlockPosY;
		this.controlBlockPosZ = controlBlockPosZ;
		this.structureIdentifier = structureIdentifier !=  null ? structureIdentifier : "";
		this.displayName = displayName !=  null ? displayName : "";
		this.unlockAdvancement = unlockAdvancement !=  null ? unlockAdvancement : "";
		this.lockAdvancement = lockAdvancement !=  null ? lockAdvancement : "";
		this.showLockedLocation = showLockedLocation;
		this.showUnlockAdvancement = showUnlockAdvancement;
		this.showLockAdvancement = showLockAdvancement;
//		this.showLocationName = showLocationName;
		this.showLocationOwner = showLocationOwner;
		this.isPublic = isPublic;
//		this.canOwnerBeChosen = canOwnerBeChosen;
		this.consumeKey = consumeKey;
		this.keyItemIdentifier = keyItemIdentifier !=  null ? keyItemIdentifier : "";
		this.keyItemCount = keyItemCount;
		this.side_entrances = side_entrances != null ? side_entrances : new HashMap<>();
	}

	public record SideEntrance(
			String identifier,
			String name,
			String unlockAdvancement,
			String lockAdvancement,
			boolean showLockedLocation,
			boolean showUnlockAdvancement,
			boolean showLockAdvancement,
//			boolean showLocationName,
			boolean showLocationOwner,
			boolean consumeKey,
			String keyItemIdentifier,
			int keyItemCount
	) {

		public static final Codec<SideEntrance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.optionalFieldOf("identifier", "").forGetter(x -> x.identifier),
				Codec.STRING.optionalFieldOf("name", "").forGetter(x -> x.name),
				Codec.STRING.optionalFieldOf("lockAdvancement", "").forGetter(x -> x.lockAdvancement),
				Codec.STRING.optionalFieldOf("unlockAdvancement", "").forGetter(x -> x.unlockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockedLocation", true).forGetter(x -> x.showLockedLocation),
				Codec.BOOL.optionalFieldOf("showUnlockAdvancement", true).forGetter(x -> x.showUnlockAdvancement),
				Codec.BOOL.optionalFieldOf("showLockAdvancement", true).forGetter(x -> x.showLockAdvancement),
//				Codec.BOOL.optionalFieldOf("showLocationName", true).forGetter(x -> x.showLocationName),
				Codec.BOOL.optionalFieldOf("showLocationOwner", true).forGetter(x -> x.showLocationOwner),
				Codec.BOOL.optionalFieldOf("consumeKey", true).forGetter(x -> x.consumeKey),
				Codec.STRING.optionalFieldOf("keyItemIdentifier", "").forGetter(x -> x.keyItemIdentifier),
				Codec.INT.optionalFieldOf("keyItemCount", 0).forGetter(x -> x.keyItemCount)
		).apply(instance, SideEntrance::new));

		public SideEntrance(
				String identifier,
				String name,
				String unlockAdvancement,
				String lockAdvancement,
				boolean showLockedLocation,
				boolean showUnlockAdvancement,
				boolean showLockAdvancement,
//				boolean showLocationName,
				boolean showLocationOwner,
				boolean consumeKey,
				String keyItemIdentifier,
				int keyItemCount
		) {
			this.identifier = identifier !=  null ? identifier : "";
			this.name = name !=  null ? name : "";
			this.unlockAdvancement = unlockAdvancement !=  null ? unlockAdvancement : "";
			this.lockAdvancement = lockAdvancement !=  null ? lockAdvancement : "";
			this.showLockedLocation = showLockedLocation;
			this.showUnlockAdvancement = showUnlockAdvancement;
			this.showLockAdvancement = showLockAdvancement;
//			this.showLocationName = showLocationName;
			this.showLocationOwner = showLocationOwner;
			this.consumeKey = consumeKey;
			this.keyItemIdentifier = keyItemIdentifier !=  null ? keyItemIdentifier : "";
			this.keyItemCount = keyItemCount;
		}
	}
}
