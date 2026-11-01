package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import com.github.theredbrain.scriptblocks.data.OptionalBlockPosition;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataAttachments;
import net.minecraft.world.entity.Mob;
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

	// region getter/setter
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

	public static float getBossHealthThreshold(Mob mob) {
		return mob.getAttachedOrElse(ScriptBlocksDataAttachments.BOSS_HEALTH_THRESHOLD, -1.0F);
	}

	public static void setBossHealthThreshold(Mob mob, float healthThreshold) {
		mob.setAttached(ScriptBlocksDataAttachments.BOSS_HEALTH_THRESHOLD, healthThreshold);
	}

	public static int getBossPhase(Mob mob) {
		return mob.getAttachedOrElse(ScriptBlocksDataAttachments.BOSS_PHASE, -1);
	}

	public static void setBossPhase(Mob mob, int phaseIndex) {
		mob.setAttached(ScriptBlocksDataAttachments.BOSS_PHASE, phaseIndex);
	}

	public static Optional<BlockPos> getControllerBlockPosition(Mob mob) {
		return mob.getAttachedOrElse(ScriptBlocksDataAttachments.CONTROLLER_BLOCK_POS, OptionalBlockPosition.DEFAULT).optionalBlockPos();
	}

	public static void setControllerBlockPosition(Mob mob, Optional<BlockPos> optionalBlockPosition) {
		mob.setAttached(ScriptBlocksDataAttachments.CONTROLLER_BLOCK_POS, new OptionalBlockPosition(optionalBlockPosition));
	}

	public static Optional<BlockPos> getUseRelayBlockPosition(Mob mob) {
		return mob.getAttachedOrElse(ScriptBlocksDataAttachments.USE_RELAY_BLOCK_POS, OptionalBlockPosition.DEFAULT).optionalBlockPos();
	}

	public static void setUseRelayBlockPosition(Mob mob, Optional<BlockPos> optionalBlockPosition) {
		mob.setAttached(ScriptBlocksDataAttachments.USE_RELAY_BLOCK_POS, new OptionalBlockPosition(optionalBlockPosition));
	}
	// endregion getter/setter

	// region helper
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
	// endregion helper

}
