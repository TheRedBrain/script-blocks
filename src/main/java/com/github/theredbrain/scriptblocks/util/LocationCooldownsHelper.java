package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;

public class LocationCooldownsHelper {

	public static void playerEntersLocation(PlayerEntity player, String locationIdentifier) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = locationCooldowns.cooldowns();
		cooldowns.put(locationIdentifier, (long) -1);
		DataAttachmentHelper.setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static void playerDies(PlayerEntity player, long currentTime) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = locationCooldowns.cooldowns();
		cooldowns.replaceAll((key, value) -> value == (long) -1 ? currentTime : value);
		DataAttachmentHelper.setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static void playerLeavesLocation(PlayerEntity player, String locationIdentifier, long currentTime) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = locationCooldowns.cooldowns();
		cooldowns.put(locationIdentifier, currentTime);
		DataAttachmentHelper.setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static long getCurrentLocationCooldown(PlayerEntity player, String locationIdentifier) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		return locationCooldowns.cooldowns().getOrDefault(locationIdentifier, (long) 0);
	}
}
