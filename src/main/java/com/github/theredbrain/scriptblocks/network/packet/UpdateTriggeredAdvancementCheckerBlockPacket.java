package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class UpdateTriggeredAdvancementCheckerBlockPacket implements FabricPacket {
	public static final PacketType<UpdateTriggeredAdvancementCheckerBlockPacket> TYPE = PacketType.create(
			ScriptBlocksMod.identifier("update_triggered_advancement_checker_block"),
			UpdateTriggeredAdvancementCheckerBlockPacket::new
	);

	public final BlockPos triggeredAdvancementCheckerBlockPosition;

	public final BlockPos firstTriggeredBlockPositionOffset;

	public final boolean firstTriggeredBlockResets;

	public final BlockPos secondTriggeredBlockPositionOffset;

	public final boolean secondTriggeredBlockResets;

	public final String checkedAdvancementIdentifier;

	public UpdateTriggeredAdvancementCheckerBlockPacket(BlockPos triggeredAdvancementCheckerBlockPosition, BlockPos firstTriggeredBlockPositionOffset, boolean firstTriggeredBlockResets, BlockPos secondTriggeredBlockPositionOffset, boolean secondTriggeredBlockResets, String checkedAdvancementIdentifier) {
		this.triggeredAdvancementCheckerBlockPosition = triggeredAdvancementCheckerBlockPosition;
		this.firstTriggeredBlockPositionOffset = firstTriggeredBlockPositionOffset;
		this.firstTriggeredBlockResets = firstTriggeredBlockResets;
		this.secondTriggeredBlockPositionOffset = secondTriggeredBlockPositionOffset;
		this.secondTriggeredBlockResets = secondTriggeredBlockResets;
		this.checkedAdvancementIdentifier = checkedAdvancementIdentifier;
	}

	public UpdateTriggeredAdvancementCheckerBlockPacket(PacketByteBuf buf) {
		this(buf.readBlockPos(), buf.readBlockPos(), buf.readBoolean(), buf.readBlockPos(), buf.readBoolean(), buf.readString());
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.triggeredAdvancementCheckerBlockPosition);
		buf.writeBlockPos(this.firstTriggeredBlockPositionOffset);
		buf.writeBoolean(this.firstTriggeredBlockResets);
		buf.writeBlockPos(this.secondTriggeredBlockPositionOffset);
		buf.writeBoolean(this.secondTriggeredBlockResets);
		buf.writeString(this.checkedAdvancementIdentifier);
	}

}
