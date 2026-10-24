package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.util.DataAttachmentHelper;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.network.ServerPlayerEntity;

public class EventsRegistry {
	public static void initializeEvents() {
		ServerLivingEntityEvents.AFTER_DEATH.register((livingEntity, damageSource) -> {
			if (livingEntity instanceof ServerPlayerEntity serverPlayerEntity) {
				DataAttachmentHelper.setActiveLocationCooldowns(serverPlayerEntity);
			}
		});
	}
}
