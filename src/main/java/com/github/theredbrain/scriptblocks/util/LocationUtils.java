package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.Location;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class LocationUtils {
	@Nullable
	public static Identifier unlockAdvancementForEntrance(Location location, String entrance) {
		Identifier unlockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null && !sideEntrance.unlockAdvancement().isEmpty()) {
				unlockAdvancementIdentifier = Identifier.tryParse(sideEntrance.unlockAdvancement());
			}
		}
		if (unlockAdvancementIdentifier == null && !location.unlockAdvancement().isEmpty()) {
			unlockAdvancementIdentifier = Identifier.tryParse(location.unlockAdvancement());
		}
		return unlockAdvancementIdentifier;
	}

	@Nullable
	public static Identifier lockAdvancementForEntrance(Location location, String entrance) {
		Identifier lockAdvancementIdentifier = null;
		if (location.side_entrances() != null && !entrance.isEmpty()) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null && !sideEntrance.lockAdvancement().isEmpty()) {
				lockAdvancementIdentifier = Identifier.tryParse(sideEntrance.lockAdvancement());
			}
		}
		if (lockAdvancementIdentifier == null && !location.lockAdvancement().isEmpty()) {
			lockAdvancementIdentifier = Identifier.tryParse(location.lockAdvancement());
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

	public static ItemStack getKeyForEntrance(Location location, String entrance) {
		ItemStack itemStack = ItemStack.EMPTY;
		if (entrance.isEmpty()) {
			Item item = Registries.ITEM.get(Identifier.tryParse(location.keyItemIdentifier()));
			if (item != Items.AIR) {
				itemStack = new ItemStack(item, location.keyItemCount());
			}
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				Item item = Registries.ITEM.get(Identifier.tryParse(sideEntrance.keyItemIdentifier()));
				if (item != Items.AIR) {
					itemStack = new ItemStack(item, sideEntrance.keyItemCount());
				}
			}
		}
		return itemStack;
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
