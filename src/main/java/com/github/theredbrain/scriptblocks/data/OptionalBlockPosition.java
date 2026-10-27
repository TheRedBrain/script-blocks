package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record OptionalBlockPosition(Optional<BlockPos> optionalBlockPos) {

	public static OptionalBlockPosition DEFAULT = new OptionalBlockPosition(Optional.empty());
	public static final Codec<OptionalBlockPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.optionalFieldOf("cooldowns").forGetter(x -> x.optionalBlockPos)
	).apply(instance, OptionalBlockPosition::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, OptionalBlockPosition> PACKET_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional),
			OptionalBlockPosition::optionalBlockPos,
			OptionalBlockPosition::new
	);

}
