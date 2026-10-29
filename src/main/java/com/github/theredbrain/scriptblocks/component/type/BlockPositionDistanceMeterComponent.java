package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record BlockPositionDistanceMeterComponent(
		boolean is_root_mode,
		BlockPos root_pos,
		BlockPos offset_pos,
		BlockPos offset
) implements TooltipProvider {
	public static final BlockPositionDistanceMeterComponent DEFAULT = new BlockPositionDistanceMeterComponent(true, new BlockPos(0, -70, 0), new BlockPos(0, -70, 0), BlockPos.ZERO);
	public static final Codec<BlockPositionDistanceMeterComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.BOOL.fieldOf("is_root_mode").forGetter(BlockPositionDistanceMeterComponent::is_root_mode),
							BlockPos.CODEC.fieldOf("root_pos").forGetter(BlockPositionDistanceMeterComponent::root_pos),
							BlockPos.CODEC.fieldOf("offset_pos").forGetter(BlockPositionDistanceMeterComponent::offset_pos),
							BlockPos.CODEC.fieldOf("offset").forGetter(BlockPositionDistanceMeterComponent::offset)
					)
					.apply(instance, BlockPositionDistanceMeterComponent::new)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPositionDistanceMeterComponent> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.BOOL,
			BlockPositionDistanceMeterComponent::is_root_mode,
			BlockPos.STREAM_CODEC,
			BlockPositionDistanceMeterComponent::root_pos,
			BlockPos.STREAM_CODEC,
			BlockPositionDistanceMeterComponent::offset_pos,
			BlockPos.STREAM_CODEC,
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
	public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {

		textConsumer.accept(this.is_root_mode ? Component.translatable("item.scriptblocks.block_position_distance_meter.root_mode") : Component.translatable("item.scriptblocks.block_position_distance_meter.offset_mode"));

		if (this.root_pos.getY() > -64) {
			textConsumer.accept(Component.translatable("item.scriptblocks.block_position_distance_meter.tooltip.root_pos", this.root_pos.getX(), this.root_pos.getY(), this.root_pos.getZ()));
		}
		if (this.offset_pos.getY() > -64) {
			textConsumer.accept(Component.translatable("item.scriptblocks.block_position_distance_meter.tooltip.offset_pos", this.offset_pos.getX(), this.offset_pos.getY(), this.offset_pos.getZ()));
		}
		if (this.offset.getX() != 0 || this.offset.getY() != 0 || this.offset.getZ() != 0) {
			textConsumer.accept(Component.translatable("item.scriptblocks.block_position_distance_meter.tooltip.offset", this.offset.getX(), this.offset.getY(), this.offset.getZ()));
		}
	}
}
