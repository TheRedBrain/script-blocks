package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdateDialogueBlockPacket(
		BlockPos dialogueBlockPosition,
		List<MutablePair<String, BlockPos>> dialogueUsedBlocksList,
		List<MutablePair<String, MutablePair<BlockPos, Boolean>>> dialogueTriggeredBlocksList,
		List<String> startingDialogueList,
		BlockPos dataBlockOffset
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateDialogueBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_dialogue_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDialogueBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateDialogueBlockPacket::write, UpdateDialogueBlockPacket::new);

	public UpdateDialogueBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN),
				registryByteBuf.readList(ByteBufCodecs.STRING_UTF8),
				registryByteBuf.readBlockPos()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.dialogueBlockPosition);
		registryByteBuf.writeCollection(this.dialogueUsedBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_BLOCK_POS);
		registryByteBuf.writeCollection(this.dialogueTriggeredBlocksList, CustomPacketCodecs.MUTABLE_PAIR_STRING_MUTABLE_PAIR_BLOCK_POS_BOOLEAN);
		registryByteBuf.writeCollection(this.startingDialogueList, ByteBufCodecs.STRING_UTF8);
		registryByteBuf.writeBlockPos(this.dataBlockOffset);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
