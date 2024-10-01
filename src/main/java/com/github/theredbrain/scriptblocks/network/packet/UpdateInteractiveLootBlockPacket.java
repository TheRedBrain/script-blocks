package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record UpdateInteractiveLootBlockPacket(BlockPos interactiveLootBlockPosition,
											   String lootTableIdentifierString) implements CustomPayload {
	public static final CustomPayload.Id<UpdateInteractiveLootBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_interactive_loot_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateInteractiveLootBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateInteractiveLootBlockPacket::write, UpdateInteractiveLootBlockPacket::new);

	public UpdateInteractiveLootBlockPacket(RegistryByteBuf registryByteBuf) {
		this(registryByteBuf.readBlockPos(), registryByteBuf.readString());
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.interactiveLootBlockPosition);
		registryByteBuf.writeString(this.lootTableIdentifierString);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
