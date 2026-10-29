package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import com.github.theredbrain.scriptblocks.data.OptionalBlockPosition;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataAttachments;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class DataAttachmentHelper {

	public static LocationCooldowns getLocationCooldowns(Player playerEntity) {
		return playerEntity.getAttachedOrElse(ScriptBlocksDataAttachments.LOCATION_COOLDOWNS, LocationCooldowns.DEFAULT);
	}

	public static void setLocationCooldowns(Player playerEntity, LocationCooldowns locationCooldowns) {
		playerEntity.setAttached(ScriptBlocksDataAttachments.LOCATION_COOLDOWNS, locationCooldowns);
	}

	public static Optional<BlockPos> getCurrentHousingBlockPosition(Player playerEntity) {
		return playerEntity.getAttachedOrElse(ScriptBlocksDataAttachments.CURRENT_HOUSING_BLOCK_POS, OptionalBlockPosition.DEFAULT).optionalBlockPos();
	}

	public static void setCurrentHousingBlockPosition(Player playerEntity, Optional<BlockPos> optionalBlockPosition) {
		playerEntity.setAttached(ScriptBlocksDataAttachments.CURRENT_HOUSING_BLOCK_POS, new OptionalBlockPosition(optionalBlockPosition));
	}

	@Nullable
	public static LevelData.RespawnData getCurrentLocationAccessPoint(Player playerEntity) {
		return playerEntity.getAttached(ScriptBlocksDataAttachments.CURRENT_LOCATION_ACCESS_POINT);
	}

	public static void setCurrentLocationAccessPoint(Player playerEntity, LevelData.RespawnData locationAccessPoint) {
		playerEntity.setAttached(ScriptBlocksDataAttachments.CURRENT_LOCATION_ACCESS_POINT, locationAccessPoint);
	}

	public static Optional<BlockPos> getCurrentPVPControllerBlockPosition(Player playerEntity) {
		return playerEntity.getAttachedOrElse(ScriptBlocksDataAttachments.CURRENT_PVP_CONTROLLER_BLOCK_POS, OptionalBlockPosition.DEFAULT).optionalBlockPos();
	}

	public static void setCurrentPVPControllerBlockPosition(Player playerEntity, Optional<BlockPos> optionalBlockPosition) {
		playerEntity.setAttached(ScriptBlocksDataAttachments.CURRENT_PVP_CONTROLLER_BLOCK_POS, new OptionalBlockPosition(optionalBlockPosition));
	}

	public static void playerEntersLocation(ServerPlayer player, String locationIdentifier) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = new HashMap<>(locationCooldowns.cooldowns());
		cooldowns.put(locationIdentifier, -1L);
		setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static void setActiveLocationCooldowns(ServerPlayer player) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = new HashMap<>(locationCooldowns.cooldowns());
		cooldowns.replaceAll((key, value) -> value == -1L ? player.level().getServer().overworld().getGameTime() : value);
		setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static void playerLeavesLocation(ServerPlayer player, String locationIdentifier) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		Map<String, Long> cooldowns = locationCooldowns.cooldowns();
		cooldowns.put(locationIdentifier, player.level().getServer().overworld().getGameTime());
		setLocationCooldowns(player, new LocationCooldowns(cooldowns));
	}

	public static long getCurrentLocationCooldown(Player player, String locationIdentifier) {
		LocationCooldowns locationCooldowns = DataAttachmentHelper.getLocationCooldowns(player);
		return locationCooldowns.cooldowns().getOrDefault(locationIdentifier, -2L);
	}

}
