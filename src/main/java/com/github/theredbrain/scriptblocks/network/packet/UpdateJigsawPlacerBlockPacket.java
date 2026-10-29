package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import com.github.theredbrain.scriptblocks.util.CustomPacketCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public record UpdateJigsawPlacerBlockPacket(
		BlockPos jigsawPlacerBlockPosition,
		String structurePoolString,
		List<MutablePair<BlockPos, MutablePair<String, String>>> structurePoolStringAppendices,
		String target,
		JigsawBlockEntity.JointType joint,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateJigsawPlacerBlockPacket> PACKET_ID = new CustomPacketPayload.Type<>(ScriptBlocks.identifier("update_jigsaw_placer_block"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateJigsawPlacerBlockPacket> PACKET_CODEC = StreamCodec.ofMember(UpdateJigsawPlacerBlockPacket::write, UpdateJigsawPlacerBlockPacket::new);

	public UpdateJigsawPlacerBlockPacket(RegistryFriendlyByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readUtf(),
				registryByteBuf.readList(CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_STRING_STRING),
				registryByteBuf.readUtf(),
				JigsawBlockEntity.JointType.byName(registryByteBuf.readUtf()).orElse(JigsawBlockEntity.JointType.ALIGNED),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean()
		);
	}

	private void write(RegistryFriendlyByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.jigsawPlacerBlockPosition);
		registryByteBuf.writeUtf(this.structurePoolString);
		registryByteBuf.writeCollection(this.structurePoolStringAppendices, CustomPacketCodecs.MUTABLE_PAIR_BLOCK_POS_MUTABLE_PAIR_STRING_STRING);
		registryByteBuf.writeUtf(this.target);
		registryByteBuf.writeUtf(this.joint.getSerializedName());
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
