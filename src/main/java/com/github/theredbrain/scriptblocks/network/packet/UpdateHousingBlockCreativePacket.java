package com.github.theredbrain.scriptblocks.network.packet;

import com.github.theredbrain.scriptblocks.ScriptBlocksMod;
import com.github.theredbrain.scriptblocks.block.entity.HousingBlockEntity;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class UpdateHousingBlockCreativePacket implements FabricPacket {
	public static final PacketType<UpdateHousingBlockCreativePacket> TYPE = PacketType.create(
			ScriptBlocksMod.identifier("update_housing_block_creative"),
			UpdateHousingBlockCreativePacket::new
	);

	public final BlockPos housingBlockPosition;
	public final boolean showRestrictBlockBreakingArea;
	public final Vec3i restrictBlockBreakingAreaDimensions;
	public final BlockPos restrictBlockBreakingAreaPositionOffset;
	public final BlockPos triggeredBlockPositionOffset;
	public final boolean triggeredBlockResets;
	public final HousingBlockEntity.OwnerMode ownerMode;

	public UpdateHousingBlockCreativePacket(BlockPos housingBlockPosition, boolean showRestrictBlockBreakingArea, Vec3i restrictBlockBreakingAreaDimensions, BlockPos restrictBlockBreakingAreaPositionOffset, BlockPos triggeredBlockPositionOffset, boolean triggeredBlockResets, String ownerMode) {
		this.housingBlockPosition = housingBlockPosition;
		this.showRestrictBlockBreakingArea = showRestrictBlockBreakingArea;
		this.restrictBlockBreakingAreaDimensions = restrictBlockBreakingAreaDimensions;
		this.restrictBlockBreakingAreaPositionOffset = restrictBlockBreakingAreaPositionOffset;
		this.triggeredBlockPositionOffset = triggeredBlockPositionOffset;
		this.triggeredBlockResets = triggeredBlockResets;
		this.ownerMode = HousingBlockEntity.OwnerMode.byName(ownerMode).orElseGet(() -> HousingBlockEntity.OwnerMode.DIMENSION_OWNER);
	}

	public UpdateHousingBlockCreativePacket(PacketByteBuf buf) {
		this(
				buf.readBlockPos(),
				buf.readBoolean(),
				new Vec3i(
						buf.readInt(),
						buf.readInt(),
						buf.readInt()
				),
				buf.readBlockPos(),
				buf.readBlockPos(),
				buf.readBoolean(),
				buf.readString()
		);
	}

	@Override
	public PacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(PacketByteBuf buf) {
		buf.writeBlockPos(this.housingBlockPosition);
		buf.writeBoolean(this.showRestrictBlockBreakingArea);
		buf.writeInt(this.restrictBlockBreakingAreaDimensions.getX());
		buf.writeInt(this.restrictBlockBreakingAreaDimensions.getY());
		buf.writeInt(this.restrictBlockBreakingAreaDimensions.getZ());
		buf.writeBlockPos(this.restrictBlockBreakingAreaPositionOffset);
		buf.writeBlockPos(this.triggeredBlockPositionOffset);
		buf.writeBoolean(this.triggeredBlockResets);
		buf.writeString(this.ownerMode.asString());
	}

}
