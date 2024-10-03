package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.Location;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LocationUtils {
	public static Identifier unlockAdvancementForEntrance(Location location, String entrance) {
		Identifier unlockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				unlockAdvancementIdentifier = sideEntrance.unlockAdvancement();
			}
		}
		if (unlockAdvancementIdentifier == null) {
			unlockAdvancementIdentifier = location.unlockAdvancement();
		}
		return unlockAdvancementIdentifier;
	}

	public static Identifier lockAdvancementForEntrance(Location location, String entrance) {
		Identifier lockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				lockAdvancementIdentifier = sideEntrance.lockAdvancement();
			}
		}
		if (lockAdvancementIdentifier == null) {
			lockAdvancementIdentifier = location.lockAdvancement();
		}
		return lockAdvancementIdentifier;
	}

	public static boolean showLockedLocationForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.showLockedLocation();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.showLockedLocation();
			}
		}
		return false;
	}

	public static boolean showUnlockAdvancementForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.showUnlockAdvancement();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.showUnlockAdvancement();
			}
		}
		return false;
	}

	public static boolean showLockAdvancementForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.showLockAdvancement();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.showLockAdvancement();
			}
		}
		return false;
	}

	public static boolean showLocationNameForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.showLocationName();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.showLocationName();
			}
		}
		return false;
	}

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

	@Nullable
	public static ItemStack getKeyForEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.key();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.key();
			}
		}
		return null;
	}

	public static boolean consumeKeyAtEntrance(Location location, String entrance) {
		if (entrance.isEmpty()) {
			return location.consumeKey();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				return sideEntrance.consumeKey();
			}
		}
		return false;
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
