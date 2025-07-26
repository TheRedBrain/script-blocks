package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LocationUtils {
	public static BlockPos getControlBlockPosForLocation(Location location) {
		return new BlockPos(location.controlBlockPos());
	}

	@Nullable
	public static Identifier unlockAdvancementForEntrance(Location location, String entrance) {
		Identifier unlockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null && !sideEntrance.availability().unlockAdvancement().isEmpty()) {
				unlockAdvancementIdentifier = Identifier.tryParse(sideEntrance.availability().unlockAdvancement());
			}
		}
		if (unlockAdvancementIdentifier == null && !location.availability().unlockAdvancement().isEmpty()) {
			unlockAdvancementIdentifier = Identifier.tryParse(location.availability().unlockAdvancement());
		}
		return unlockAdvancementIdentifier;
	}

	@Nullable
	public static Identifier lockAdvancementForEntrance(Location location, String entrance) {
		Identifier lockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null && !sideEntrance.availability().lockAdvancement().isEmpty()) {
				lockAdvancementIdentifier = Identifier.tryParse(sideEntrance.availability().lockAdvancement());
			}
		}
		if (lockAdvancementIdentifier == null && !location.availability().lockAdvancement().isEmpty()) {
			lockAdvancementIdentifier = Identifier.tryParse(location.availability().lockAdvancement());
		}
		return lockAdvancementIdentifier;
	}

	public static boolean showLockedLocationForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.availability().showLockedLocation();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.availability().showLockedLocation();
			}
		}
		return false;
	}

//	public static boolean showUnlockAdvancementForEntrance(Location location, String entrance) {
//		if (entrance.isEmpty()) {
//			return location.showUnlockAdvancement();
//		}
//		if (location.side_entrances() != null) {
//			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
//			if (sideEntrance != null) {
//				return sideEntrance.showUnlockAdvancement();
//			}
//		}
//		return false;
//	}

//	public static boolean showLockAdvancementForEntrance(Location location, String entrance) {
//		if (entrance.isEmpty()) {
//			return location.showLockAdvancement();
//		}
//		if (location.side_entrances() != null) {
//			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
//			if (sideEntrance != null) {
//				return sideEntrance.showLockAdvancement();
//			}
//		}
//		return false;
//	}

	public static boolean showLocationOwnerForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.showLocationOwner();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.showLocationOwner();
			}
		}
		return false;
	}

	public static List<CommonDataStructures.ItemCost> getKeyForEntrance(Location location, String entrance) {

		List<CommonDataStructures.ItemCost> itemCostList = new ArrayList<>();
		if (entrance.isEmpty()) {
			itemCostList = location.availability().itemCosts();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				itemCostList = sideEntrance.availability().itemCosts();
			}
		}
		return itemCostList;
	}

	public static boolean hasEntrance(Location location, String entrance) {
		if (location.side_entrances() != null) {
			return location.side_entrances().containsKey(entrance);
		}
		return false;
	}

	public static boolean hasSideEntrances(Location location) {
		return location.side_entrances() != null;
	}

	public static String getEntranceDisplayName(Location location, String entrance) {
		String entranceDisplayName = "";
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				entranceDisplayName = sideEntrance.name();
			}
		}
		return entranceDisplayName;
	}
}
