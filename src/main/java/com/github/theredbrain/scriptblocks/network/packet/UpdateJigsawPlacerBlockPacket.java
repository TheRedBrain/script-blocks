package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class UpdateJigsawPlacerBlockPacket implements FabricPacket {
	public static final PacketType<UpdateJigsawPlacerBlockPacket> TYPE = PacketType.create(
			ScriptBlocks.identifier("update_jigsaw_placer_block"),
			UpdateJigsawPlacerBlockPacket::new
	);

	public final BlockPos jigsawPlacerBlockPosition;
	public final String target;
	public final String pool;
	public final JigsawBlockEntity.Joint joint;
	public final BlockPos triggeredBlockPositionOffset;
	public final boolean triggeredBlockResets;

	public UpdateJigsawPlacerBlockPacket(BlockPos jigsawPlacerBlockPosition, String target, String pool, JigsawBlockEntity.Joint joint, BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets) {
		this.jigsawPlacerBlockPosition = jigsawPlacerBlockPosition;
		this.target = target;
		this.pool = pool;
		this.joint = joint;
		this.triggeredBlockPositionOffset = triggeredBlockPositionOffset;
		this.triggeredBlockResets = triggeredBlockResets;
	}

	public UpdateJigsawPlacerBlockPacket(PacketByteBuf buf) {
		this(buf.readBlockPos(), buf.readString(), buf.readString(), JigsawBlockEntity.Joint.byName(buf.readString()).orElse(JigsawBlockEntity.Joint.ALIGNED), buf.readBlockPos(), buf.readBoolean());
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.jigsawPlacerBlockPosition);
		buf.writeString(this.target);
		buf.writeString(this.pool);
		buf.writeString(this.joint.asString());
		buf.writeBlockPos(this.triggeredBlockPositionOffset);
		buf.writeBoolean(this.triggeredBlockResets);
	}

}
