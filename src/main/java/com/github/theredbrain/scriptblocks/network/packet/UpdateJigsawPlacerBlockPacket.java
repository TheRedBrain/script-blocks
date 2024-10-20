package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record UpdateJigsawPlacerBlockPacket(
		BlockPos jigsawPlacerBlockPosition,
		String target,
		List<String> structurePoolList,
		JigsawBlockEntity.Joint joint,
		BlockPos triggeredBlockPositionOffset,
		boolean triggeredBlockResets,
		BlockPos dataSavingBlockPosOffset,
		String checkedDataId
) implements CustomPayload {
	public static final CustomPayload.Id<UpdateJigsawPlacerBlockPacket> PACKET_ID = new CustomPayload.Id<>(ScriptBlocks.identifier("update_jigsaw_placer_block"));
	public static final PacketCodec<RegistryByteBuf, UpdateJigsawPlacerBlockPacket> PACKET_CODEC = PacketCodec.of(UpdateJigsawPlacerBlockPacket::write, UpdateJigsawPlacerBlockPacket::new);

	public UpdateJigsawPlacerBlockPacket(RegistryByteBuf registryByteBuf) {
		this(
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString(),
				registryByteBuf.readList(PacketCodecs.STRING),
				JigsawBlockEntity.Joint.byName(registryByteBuf.readString()).orElse(JigsawBlockEntity.Joint.ALIGNED),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readBoolean(),
				registryByteBuf.readBlockPos(),
				registryByteBuf.readString()
		);
	}

	private void write(RegistryByteBuf registryByteBuf) {
		registryByteBuf.writeBlockPos(this.jigsawPlacerBlockPosition);
		registryByteBuf.writeString(this.target);
		registryByteBuf.writeCollection(this.structurePoolList, PacketCodecs.STRING);
		registryByteBuf.writeString(this.joint.asString());
		registryByteBuf.writeBlockPos(this.triggeredBlockPositionOffset);
		registryByteBuf.writeBoolean(this.triggeredBlockResets);
		registryByteBuf.writeBlockPos(this.dataSavingBlockPosOffset);
		registryByteBuf.writeString(this.checkedDataId);
	}

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
