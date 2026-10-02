package com.github.theredbrain.scriptblocks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.HashMap;
import java.util.Map;

public record LocationCooldowns(
		Map<String, Long> cooldowns
) {

	public static LocationCooldowns DEFAULT = new LocationCooldowns(new HashMap<>());
	public static final Codec<LocationCooldowns> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("cooldowns").forGetter(x -> x.cooldowns)
	).apply(instance, LocationCooldowns::new));
	public static final PacketCodec<RegistryByteBuf, LocationCooldowns> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.map(Object2ObjectOpenHashMap::new, PacketCodecs.STRING, PacketCodecs.VAR_LONG),
			LocationCooldowns::cooldowns,
			LocationCooldowns::new
	);

}
