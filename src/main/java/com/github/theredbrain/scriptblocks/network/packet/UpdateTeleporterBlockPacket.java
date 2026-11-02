package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.block.entity.TeleporterBlockEntity;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.storage.LevelData;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateTeleporterBlockPacket(
		BlockPos teleportBlockPosition,
		TeleporterBlockEntity.CreativeScreenPage creativeScreenPage,
		boolean showAdventureScreen,
		String preventInteractionIdentifierString,
		String interactionPreventedMessage,
		BlockPos uuidListHandlerBlockPositionOffset,
		LevelData.RespawnData locationAccessPoint,
		boolean enableLocationAccessPoint,
		boolean onlyTeleportDimensionOwner,
		String onlyDimensionOwnerCanTeleportMessage,
		boolean teleportTeam,
		TeleporterBlockEntity.TeleportationMode teleportationMode,
		LevelData.RespawnData directTeleportTarget,
		TeleporterBlockEntity.SpawnPointType spawnPointType,
		List<MutablePair<MutablePair<String, String>, MutablePair<String, String>>> locationsList,
		MutablePair<MutablePair<String, String>, MutablePair<String, String>> location,
		BlockPos dataProvidingBlockPosOffset,
		String locationDataIdentifier,
		String entranceDataIdentifier,
		String sendDataIdentifierDataIdentifier,
		String sendDataValueDataIdentifier,
		BlockPos preTeleportTriggeredBlockPositionOffset,
		boolean preTeleportTriggeredBlockResets,
		BlockPos onTeleportUUIDListHandlerPositionOffset,
		BlockPos postTeleportTriggeredBlockPositionOffset,
		boolean postTeleportTriggeredBlockResets,
		String teleporterName,
		String currentTargetIdentifierLabel,
		String currentTargetOwnerLabel,
		boolean showCancelButton,
		boolean canOwnerBeChosen,
		String teleportButtonLabel,
		String cancelTeleportButtonLabel
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateTeleporterBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_teleporter_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTeleporterBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateTeleporterBlockPacket::write, UpdateTeleporterBlockPacket::new);

	public UpdateTeleporterBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readEnum(TeleporterBlockEntity.CreativeScreenPage.class),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBlockPos(),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readEnum(TeleporterBlockEntity.TeleportationMode.class),
				LevelData.RespawnData.STREAM_CODEC.decode(registryByteBuf),
				registryByteBuf.readEnum(TeleporterBlockEntity.SpawnPointType.class),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING),
				CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING.decode(registryByteBuf),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.teleportBlockPosition);

		registryByteBuf.writeEnum(this.creativeScreenPage);

		registryByteBuf.writeBoolean(this.showAdventureScreen);

		registryByteBuf.writeUtf(this.preventInteractionIdentifierString);
		registryByteBuf.writeUtf(this.interactionPreventedMessage);

		registryByteBuf.writeBlockPos(this.uuidListHandlerBlockPositionOffset);

		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.locationAccessPoint);
		registryByteBuf.writeBoolean(this.enableLocationAccessPoint);

		registryByteBuf.writeBoolean(this.onlyTeleportDimensionOwner);
		registryByteBuf.writeUtf(this.onlyDimensionOwnerCanTeleportMessage);

		registryByteBuf.writeBoolean(this.teleportTeam);

		registryByteBuf.writeEnum(this.teleportationMode);

		LevelData.RespawnData.STREAM_CODEC.encode(registryByteBuf, this.directTeleportTarget);

		registryByteBuf.writeEnum(this.spawnPointType);

		registryByteBuf.writeCollection(this.locationsList, CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING);

		CustomPacketCodecs.MUTABLE_PAIR_MUTABLE_PAIR_STRING_STRING_MUTABLE_PAIR_STRING_STRING.encode(registryByteBuf, this.location);

		registryByteBuf.writeBlockPos(this.dataProvidingBlockPosOffset);

		registryByteBuf.writeUtf(this.locationDataIdentifier);
		registryByteBuf.writeUtf(this.entranceDataIdentifier);
		registryByteBuf.writeUtf(this.sendDataIdentifierDataIdentifier);
		registryByteBuf.writeUtf(this.sendDataValueDataIdentifier);

		registryByteBuf.writeBlockPos(this.preTeleportTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.preTeleportTriggeredBlockResets);
		registryByteBuf.writeBlockPos(this.onTeleportUUIDListHandlerPositionOffset);
		registryByteBuf.writeBlockPos(this.postTeleportTriggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.postTeleportTriggeredBlockResets);

		registryByteBuf.writeUtf(this.teleporterName);
		registryByteBuf.writeUtf(this.currentTargetIdentifierLabel);
		registryByteBuf.writeUtf(this.currentTargetOwnerLabel);
		registryByteBuf.writeBoolean(this.showCancelButton);
		registryByteBuf.writeBoolean(this.canOwnerBeChosen);
		registryByteBuf.writeUtf(this.teleportButtonLabel);
		registryByteBuf.writeUtf(this.cancelTeleportButtonLabel);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
