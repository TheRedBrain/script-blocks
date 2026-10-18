package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public record OptionalBlockPosition(Optional<BlockPos> optionalBlockPos) {

	public static OptionalBlockPosition DEFAULT = new OptionalBlockPosition(Optional.empty());
	public static final Codec<OptionalBlockPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.optionalFieldOf("cooldowns").forGetter(x -> x.optionalBlockPos)
	).apply(instance, OptionalBlockPosition::new));
	public static final PacketCodec<RegistryByteBuf, OptionalBlockPosition> PACKET_CODEC = PacketCodec.tuple(
			BlockPos.PACKET_CODEC.collect(PacketCodecs::optional),
			OptionalBlockPosition::optionalBlockPos,
			OptionalBlockPosition::new
	);

}
