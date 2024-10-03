package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;

public record BlockPositionDistanceMeterComponent(
		boolean is_root_mode,
		BlockPos root_pos,
		BlockPos offset_pos,
		BlockPos offset
) implements TooltipAppender {
	public static final BlockPositionDistanceMeterComponent DEFAULT = new BlockPositionDistanceMeterComponent(true, new BlockPos(0, -70, 0), new BlockPos(0, -70, 0), BlockPos.ORIGIN);
	public static final Codec<BlockPositionDistanceMeterComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.BOOL.fieldOf("is_root_mode").forGetter(BlockPositionDistanceMeterComponent::is_root_mode),
							BlockPos.CODEC.fieldOf("root_pos").forGetter(BlockPositionDistanceMeterComponent::root_pos),
							BlockPos.CODEC.fieldOf("offset_pos").forGetter(BlockPositionDistanceMeterComponent::offset_pos),
							BlockPos.CODEC.fieldOf("offset").forGetter(BlockPositionDistanceMeterComponent::offset)
					)
					.apply(instance, BlockPositionDistanceMeterComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, BlockPositionDistanceMeterComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL,
			BlockPositionDistanceMeterComponent::is_root_mode,
			BlockPos.PACKET_CODEC,
			BlockPositionDistanceMeterComponent::root_pos,
			BlockPos.PACKET_CODEC,
			BlockPositionDistanceMeterComponent::offset_pos,
			BlockPos.PACKET_CODEC,
			BlockPositionDistanceMeterComponent::offset,
			BlockPositionDistanceMeterComponent::new
	);

	public BlockPositionDistanceMeterComponent(
			boolean is_root_mode,
			BlockPos root_pos,
			BlockPos offset_pos,
			BlockPos offset
	) {
		this.is_root_mode = is_root_mode;
		this.root_pos = root_pos;
		this.offset_pos = offset_pos;
		this.offset = offset;
	}

	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> tooltip, TooltipType type) {

		tooltip.accept(this.is_root_mode ? Text.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Text.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

		if (this.root_pos.getY() > -64) {
			tooltip.accept(Text.translatable("item.scriptblocks.block_position_distance_meter.tooltip.root_pos", this.root_pos.getX(), this.root_pos.getY(), this.root_pos.getZ()));
		}
		if (this.offset_pos.getY() > -64) {
			tooltip.accept(Text.translatable("item.scriptblocks.block_position_distance_meter.tooltip.offset_pos", this.offset_pos.getX(), this.offset_pos.getY(), this.offset_pos.getZ()));
		}
		if (this.offset.getX() != 0 || this.offset.getY() != 0 || this.offset.getZ() != 0) {
			tooltip.accept(Text.translatable("item.scriptblocks.block_position_distance_meter.tooltip.offset", this.offset.getX(), this.offset.getY(), this.offset.getZ()));
		}
	}
}
