package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocationUtils {
	public static BlockPos getControlBlockPosForLocation(Location location) {
		return new BlockPos(location.controlBlockPos());
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

	public static Text getCooldownText(long cooldown) {
		int seconds = Math.toIntExact(cooldown / 20);
		if (seconds < 60) {
			return Text.translatable("gui.teleporter_block.cooldown_seconds", seconds);
		} else {
			int minutes = seconds / 60;
			seconds = seconds % 60;
			if (minutes < 60) {
				return Text.translatable("gui.teleporter_block.cooldown_minutes", minutes, seconds);
			} else {
				int hours = minutes / 60;
				minutes = minutes % 60;
				return Text.translatable("gui.teleporter_block.cooldown_hours", hours, minutes, seconds);
			}
		}
	}

	public static boolean isLocationEntranceUnlocked(ServerPlayerEntity serverPlayerEntity, Location location, String entrance) {
		Optional<LootContextPredicate> optionalEntityPredicate = Optional.empty();
		if (entrance.isEmpty()) {
			optionalEntityPredicate = location.availability().unlock_predicate();
		} else if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				optionalEntityPredicate = sideEntrance.availability().unlock_predicate();
			}
		}

		if (optionalEntityPredicate.isPresent()) {
			return optionalEntityPredicate.get().test(EntityPredicate.createAdvancementEntityLootContext(serverPlayerEntity, serverPlayerEntity));
		}

		return true;
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

	public static long getRemainingCooldownForEntrance(ServerPlayerEntity serverPlayerEntity, String locationIdentifier, String entrance) {

		Location location = null;
		Optional<RegistryEntry.Reference<Location>> optionalLocationReference = serverPlayerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(locationIdentifier));
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}
		long cooldown = 0L;
		if (location == null) {
			return cooldown;
		}
		if (serverPlayerEntity.getServer() == null) {
			return cooldown;
		}
		if (entrance.isEmpty()) {
			cooldown = location.availability().cooldown();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				cooldown = sideEntrance.availability().cooldown();
			}
		}
		long currentTime = serverPlayerEntity.getServer().getOverworld().getTime();
		long currentLocationCooldownStart = DataAttachmentHelper.getCurrentLocationCooldown(serverPlayerEntity, locationIdentifier);
		if (currentLocationCooldownStart < 0) {
			return 0;
		}
		return Math.max(0, cooldown - (currentTime - currentLocationCooldownStart));
	}

	public static long getCooldownForEntrance(PlayerEntity playerEntity, String locationIdentifier, String entrance) {

		Location location = null;
		Optional<RegistryEntry.Reference<Location>> optionalLocationReference = playerEntity.getWorld().getRegistryManager().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).getEntry(Identifier.tryParse(locationIdentifier));
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}
		long cooldown = 0L;
		if (location == null) {
			return cooldown;
		}
		if (entrance.isEmpty()) {
			cooldown = location.availability().cooldown();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				cooldown = sideEntrance.availability().cooldown();
			}
		}
		return cooldown;
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

	public static boolean isEntranceNameShownFirst(Location location, String entrance) {
		boolean isEntranceNameShownFirst = true;
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				isEntranceNameShownFirst = sideEntrance.isEntranceNameShownFirst();
			}
		}
		return isEntranceNameShownFirst;
	}

	public static boolean allowRegeneration(Location location, String entrance) {
		boolean allowRegeneration = location.allowRegeneration();
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				allowRegeneration = sideEntrance.allowRegeneration();
			}
		}
		return allowRegeneration;
	}
}
