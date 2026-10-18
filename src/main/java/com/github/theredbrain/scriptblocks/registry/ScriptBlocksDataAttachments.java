package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ScriptBlocksDataAttachments {
	public static AttachmentType<LocationCooldowns> LOCATION_COOLDOWNS;

	public static void bootstrap() {
	}

	static {
		LOCATION_COOLDOWNS = AttachmentRegistry.create(ScriptBlocks.identifier("location_cooldowns"), builder -> builder
				.persistent(LocationCooldowns.CODEC)
				.syncWith(LocationCooldowns.PACKET_CODEC, AttachmentSyncPredicate.all())
		);
	}
}
