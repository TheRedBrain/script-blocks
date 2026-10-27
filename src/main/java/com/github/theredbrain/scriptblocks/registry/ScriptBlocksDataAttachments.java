package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import com.github.theredbrain.scriptblocks.data.OptionalBlockPosition;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;

public class ScriptBlocksDataAttachments {
	public static AttachmentType<LocationCooldowns> LOCATION_COOLDOWNS;
	public static AttachmentType<OptionalBlockPosition> CURRENT_HOUSING_BLOCK_POS;
	public static AttachmentType<OptionalBlockPosition> CURRENT_LOCATION_ACCESS_BLOCK_POS;
	public static AttachmentType<String> CURRENT_LOCATION_ACCESS_DIMENSION;
	public static AttachmentType<OptionalBlockPosition> CURRENT_PVP_CONTROLLER_BLOCK_POS;

	public static void bootstrap() {
	}

	static {
		LOCATION_COOLDOWNS = AttachmentRegistry.create(ScriptBlocks.identifier("location_cooldowns"), builder -> builder
				.persistent(LocationCooldowns.CODEC)
				.copyOnDeath()
				.syncWith(LocationCooldowns.PACKET_CODEC, AttachmentSyncPredicate.all())
		);
		CURRENT_HOUSING_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("current_housing_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.PACKET_CODEC, AttachmentSyncPredicate.all())
		);
		CURRENT_LOCATION_ACCESS_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("current_location_access_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.PACKET_CODEC, AttachmentSyncPredicate.all())
		);
		CURRENT_LOCATION_ACCESS_DIMENSION = AttachmentRegistry.create(ScriptBlocks.identifier("current_location_access_dimension"), builder -> builder
				.persistent(Codec.STRING)
				.copyOnDeath()
				.syncWith(ByteBufCodecs.STRING_UTF8, AttachmentSyncPredicate.all())
		);
		CURRENT_PVP_CONTROLLER_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("current_pvp_controller_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.PACKET_CODEC, AttachmentSyncPredicate.all())
		);
	}
}
