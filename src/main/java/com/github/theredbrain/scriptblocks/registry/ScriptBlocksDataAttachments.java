package com.github.theredbrain.scriptblocks.registry;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.data.LocationCooldowns;
import com.github.theredbrain.scriptblocks.data.OptionalBlockPosition;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.level.storage.LevelData;

public class ScriptBlocksDataAttachments {
	// player
	public static AttachmentType<LocationCooldowns> LOCATION_COOLDOWNS;
	public static AttachmentType<OptionalBlockPosition> CURRENT_HOUSING_BLOCK_POS;
	public static AttachmentType<LevelData.RespawnData> CURRENT_LOCATION_ACCESS_POINT;
	public static AttachmentType<OptionalBlockPosition> CURRENT_PVP_CONTROLLER_BLOCK_POS;

	// mob
	public static AttachmentType<Float> BOSS_HEALTH_THRESHOLD;
	public static AttachmentType<Integer> BOSS_PHASE;
	public static AttachmentType<OptionalBlockPosition> CONTROLLER_BLOCK_POS;
	public static AttachmentType<OptionalBlockPosition> USE_RELAY_BLOCK_POS;

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
				.syncWith(OptionalBlockPosition.STREAM_CODEC, AttachmentSyncPredicate.all())
		);
		CURRENT_LOCATION_ACCESS_POINT = AttachmentRegistry.create(ScriptBlocks.identifier("current_location_access_point"), builder -> builder
				.persistent(LevelData.RespawnData.CODEC)
				.copyOnDeath()
				.syncWith(LevelData.RespawnData.STREAM_CODEC, AttachmentSyncPredicate.all())
		);
		CURRENT_PVP_CONTROLLER_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("current_pvp_controller_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.STREAM_CODEC, AttachmentSyncPredicate.all())
		);
		BOSS_HEALTH_THRESHOLD = AttachmentRegistry.create(ScriptBlocks.identifier("boss_health_threshold"), builder -> builder
				.persistent(Codec.FLOAT)
				.copyOnDeath()
				.syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.all())
		);
		BOSS_PHASE = AttachmentRegistry.create(ScriptBlocks.identifier("boss_phase"), builder -> builder
				.persistent(Codec.INT)
				.copyOnDeath()
				.syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.all())
		);
		CONTROLLER_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("controller_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.STREAM_CODEC, AttachmentSyncPredicate.all())
		);
		USE_RELAY_BLOCK_POS = AttachmentRegistry.create(ScriptBlocks.identifier("use_relay_block_pos"), builder -> builder
				.persistent(OptionalBlockPosition.CODEC)
				.copyOnDeath()
				.syncWith(OptionalBlockPosition.STREAM_CODEC, AttachmentSyncPredicate.all())
		);
	}
}
