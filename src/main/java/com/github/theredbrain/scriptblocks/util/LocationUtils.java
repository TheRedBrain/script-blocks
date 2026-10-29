package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.CommonDataStructures;
import com.github.theredbrain.scriptblocks.data.Location;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDynamicRegistries;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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

	public static Component getCooldownText(long cooldown) {
		int seconds = Math.toIntExact(cooldown / 20);
		if (seconds < 60) {
			return Component.translatable("gui.teleporter_block.cooldown_seconds", seconds);
		} else {
			int minutes = seconds / 60;
			seconds = seconds % 60;
			if (minutes < 60) {
				return Component.translatable("gui.teleporter_block.cooldown_minutes", minutes, seconds);
			} else {
				int hours = minutes / 60;
				minutes = minutes % 60;
				return Component.translatable("gui.teleporter_block.cooldown_hours", hours, minutes, seconds);
			}
		}
	}

	public static boolean isLocationEntranceUnlocked(ServerPlayer serverPlayerEntity, Location location, String entrance) {
		Optional<ContextAwarePredicate> optionalEntityPredicate = Optional.empty();
		if (entrance.isEmpty()) {
			optionalEntityPredicate = location.availability().unlock_predicate();
		} else if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				optionalEntityPredicate = sideEntrance.availability().unlock_predicate();
			}
		}

		if (optionalEntityPredicate.isPresent()) {
			return optionalEntityPredicate.get().matches(EntityPredicate.createContext(serverPlayerEntity, serverPlayerEntity));
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

	public static List<CommonDataStructures.ItemCost> getResetCostForEntrance(Location location, String entrance) {

		List<CommonDataStructures.ItemCost> resetItemCostList = new ArrayList<>();
		if (entrance.isEmpty()) {
			resetItemCostList = location.resetItemCosts();
		}
		if (location.side_entrances() != null) {
			Location.SideEntrance sideEntrance = location.side_entrances().get(entrance);
			if (sideEntrance != null) {
				resetItemCostList = sideEntrance.resetItemCosts();
			}
		}
		return resetItemCostList;
	}

	public static long getRemainingCooldownForEntrance(ServerPlayer serverPlayerEntity, String locationIdentifier, String entrance) {

		Location location = null;
		long cooldown = 0L;
		Identifier identifier = Identifier.tryParse(locationIdentifier);
		if (identifier == null) {
			return cooldown;
		}
		Optional<Holder.Reference<Location>> optionalLocationReference = serverPlayerEntity.level().registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).get().value().get(identifier);
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}
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
		long currentTime = serverPlayerEntity.level().getServer().overworld().getGameTime();
		long currentLocationCooldownStart = DataAttachmentHelper.getCurrentLocationCooldown(serverPlayerEntity, locationIdentifier);
		if (currentLocationCooldownStart < 0) {
			return 0;
		}
		return Math.max(0, cooldown - (currentTime - currentLocationCooldownStart));
	}

	public static long getCooldownForEntrance(Player playerEntity, String locationIdentifier, String entrance) {

		Location location = null;
		long cooldown = 0L;
		Identifier identifier = Identifier.tryParse(locationIdentifier);
		if (identifier == null) {
			return cooldown;
		}
		Optional<Holder.Reference<Location>> optionalLocationReference = playerEntity.level().registryAccess().get(ScriptBlocksDynamicRegistries.LOCATION_REGISTRY_KEY).get().value().get(identifier);
		if (optionalLocationReference.isPresent()) {
			location = optionalLocationReference.get().value();
		}
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
