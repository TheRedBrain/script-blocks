package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.util.PacketByteBufUtils;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.List;

public class UpdateLocationControlBlockPacket implements FabricPacket {
	public static final PacketType<UpdateLocationControlBlockPacket> TYPE = PacketType.create(
			ScriptBlocksMod.identifier("update_location_control_block"),
			UpdateLocationControlBlockPacket::new
	);

	public final BlockPos locationControlBlockPosition;
	public final BlockPos mainEntrancePositionOffset;
	public final double mainEntranceYaw;
	public final double mainEntrancePitch;
	public final List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> sideEntrancesList;
	public final BlockPos triggeredBlockPositionOffset;
	public final boolean triggeredBlockResets;
	public final boolean shouldAlwaysReset;

	public UpdateLocationControlBlockPacket(BlockPos locationControlBlockPosition, BlockPos mainEntrancePositionOffset, double mainEntranceYaw, double mainEntrancePitch, List<MutablePair<String, MutablePair<BlockPos, MutablePair<Double, Double>>>> sideEntrancesList, BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets, boolean shouldAlwaysReset) {
		this.locationControlBlockPosition = locationControlBlockPosition;
		this.mainEntrancePositionOffset = mainEntrancePositionOffset;
		this.mainEntranceYaw = mainEntranceYaw;
		this.mainEntrancePitch = mainEntrancePitch;
		this.sideEntrancesList = sideEntrancesList;
		this.triggeredBlockPositionOffset = triggeredBlockPositionOffset;
		this.triggeredBlockResets = triggeredBlockResets;
		this.shouldAlwaysReset = shouldAlwaysReset;
	}

	public UpdateLocationControlBlockPacket(PacketByteBuf buf) {
		this(
				buf.readBlockPos(),
				buf.readBlockPos(),
				buf.readDouble(),
				buf.readDouble(),
				buf.readList(new PacketByteBufUtils.MutablePairStringMutablePairBlockPosMutablePairDoubleDoubleReader()),
				buf.readBlockPos(),
				buf.readBoolean(),
				buf.readBoolean()
		);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.locationControlBlockPosition);
		buf.writeBlockPos(this.mainEntrancePositionOffset);
		buf.writeDouble(this.mainEntranceYaw);
		buf.writeDouble(this.mainEntrancePitch);
		buf.writeCollection(this.sideEntrancesList, new PacketByteBufUtils.MutablePairStringMutablePairBlockPosMutablePairDoubleDoubleWriter());
		buf.writeBlockPos(this.triggeredBlockPositionOffset);
		buf.writeBoolean(this.triggeredBlockResets);
		buf.writeBoolean(this.shouldAlwaysReset);
	}
}
