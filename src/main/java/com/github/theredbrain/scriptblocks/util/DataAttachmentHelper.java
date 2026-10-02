package com.github.theredbrain.scriptblocks.util;

import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import com.github.theredbrain.scriptblocks.registry.ScriptBlocksDataAttachments;
import net.minecraft.entity.player.PlayerEntity;

public class DataAttachmentHelper {

	public static LocationCooldowns getLocationCooldowns(PlayerEntity playerEntity) {
		return playerEntity.getAttachedOrElse(ScriptBlocksDataAttachments.LOCATION_COOLDOWNS, LocationCooldowns.DEFAULT);
	}

	public static void setLocationCooldowns(PlayerEntity playerEntity, LocationCooldowns locationCooldowns) {
		playerEntity.setAttached(ScriptBlocksDataAttachments.LOCATION_COOLDOWNS, locationCooldowns);
	}

}
