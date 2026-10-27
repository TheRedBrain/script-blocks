package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@Deprecated
public record UpdateInteractiveLootBlockPacket(
		BlockPos interactiveLootBlockPosition,
		String lootTableIdentifierString,
		String mode,
		int rolls,
		int choices,
		boolean trackPlayers,
		String lootAcquiredMessage,
		String lootAcquiredSoundId,
		String alreadyLootedMessage,
		String alreadyLootedSoundId
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateInteractiveLootBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_interactive_loot_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateInteractiveLootBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateInteractiveLootBlockPacket::write, UpdateInteractiveLootBlockPacket::new);

	public UpdateInteractiveLootBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf(),
				registryByteBuf.readUtf()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.interactiveLootBlockPosition);
		registryByteBuf.writeUtf(this.lootTableIdentifierString);
		registryByteBuf.writeUtf(this.mode);
		registryByteBuf.writeInt(this.rolls);
		registryByteBuf.writeInt(this.choices);
		registryByteBuf.writeBoolean(this.trackPlayers);
		registryByteBuf.writeUtf(this.lootAcquiredMessage);
		registryByteBuf.writeUtf(this.lootAcquiredSoundId);
		registryByteBuf.writeUtf(this.alreadyLootedMessage);
		registryByteBuf.writeUtf(this.alreadyLootedSoundId);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
