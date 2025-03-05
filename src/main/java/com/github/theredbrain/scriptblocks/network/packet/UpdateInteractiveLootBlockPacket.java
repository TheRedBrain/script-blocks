package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

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
) implements CustomPayload {
	public static final CustomPayload.Id<UpdateInteractiveLootBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_interactive_loot_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateInteractiveLootBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateInteractiveLootBlockPacket::write, UpdateInteractiveLootBlockPacket::new);

	public UpdateInteractiveLootBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readInt(),
				registryByteBuf.readInt(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.interactiveLootBlockPosition);
		registryByteBuf.writeString(this.lootTableIdentifierString);
		registryByteBuf.writeString(this.mode);
		registryByteBuf.writeInt(this.rolls);
		registryByteBuf.writeInt(this.choices);
		registryByteBuf.writeBoolean(this.trackPlayers);
		registryByteBuf.writeString(this.lootAcquiredMessage);
		registryByteBuf.writeString(this.lootAcquiredSoundId);
		registryByteBuf.writeString(this.alreadyLootedMessage);
		registryByteBuf.writeString(this.alreadyLootedSoundId);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
