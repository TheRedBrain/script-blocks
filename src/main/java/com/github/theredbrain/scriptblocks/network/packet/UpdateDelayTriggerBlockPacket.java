package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocks;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class UpdateDelayTriggerBlockPacket implements FabricPacket {
	public static final PacketType<UpdateDelayTriggerBlockPacket> TYPE = PacketType.create(
			ScriptBlocks.identifier("update_delay_trigger_block"),
			UpdateDelayTriggerBlockPacket::new
	);

	public final BlockPos delayTriggerBlockPosition;

	public final BlockPos triggeredBlockPositionOffset;

	public final boolean triggeredBlockResets;

	public final int triggerDelay;

	public UpdateDelayTriggerBlockPacket(BlockPos delayTriggerBlockPosition, BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets, int triggerDelay) {
		this.delayTriggerBlockPosition = delayTriggerBlockPosition;
		this.triggeredBlockPositionOffset = triggeredBlockPositionOffset;
		this.triggeredBlockResets = triggeredBlockResets;
		this.triggerDelay = triggerDelay;
	}

	public UpdateDelayTriggerBlockPacket(PacketByteBuf buf) {
		this(buf.readBlockPos(), buf.readBlockPos(), buf.readBoolean(), buf.readInt());
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.delayTriggerBlockPosition);
		buf.writeBlockPos(this.triggeredBlockPositionOffset);
		buf.writeBoolean(this.triggeredBlockResets);
		buf.writeInt(this.triggerDelay);
	}

}
