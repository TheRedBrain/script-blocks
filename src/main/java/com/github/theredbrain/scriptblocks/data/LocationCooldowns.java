package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record LocationCooldowns(
		Map<String, Long> cooldowns
) {

	public static LocationCooldowns DEFAULT = new LocationCooldowns(new HashMap<>());
	public static final Codec<LocationCooldowns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("cooldowns").forGetter(x -> x.cooldowns)
	).apply(instance, LocationCooldowns::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, LocationCooldowns> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(Object2ObjectOpenHashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_LONG),
			LocationCooldowns::cooldowns,
			LocationCooldowns::new
	);

}
