package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateDialogueBlockPacket(BlockPos dialogueBlockPosition,
										List<MutablePair<String, BlockPos>> dialogueUsedBlocksList,
										List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList,
										List<String> startingDialogueList) implements CustomPayload {
	public static final CustomPayload.Id<UpdateDialogueBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_dialogue_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateDialogueBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateDialogueBlockPacket::write, UpdateDialogueBlockPacket::new);

	public UpdateDialogueBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN),
				registryByteBuf.readList(PacketCodecs.STRING)
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dialogueBlockPosition);
		registryByteBuf.writeCollection(this.dialogueUsedBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
		registryByteBuf.writeCollection(this.dialogueTriggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
		registryByteBuf.writeCollection(this.startingDialogueList, PacketCodecs.STRING);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
