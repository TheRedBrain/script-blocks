package com.github.theredbrain.scriptblocks.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record InteractiveKeyComponent(
		boolean is_consumed,
		List<Identifier> identifier_list
) {
	public static final InteractiveKeyComponent DEFAULT = new InteractiveKeyComponent(false, new ArrayList<>());
	public static final Codec<InteractiveKeyComponent> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
							Codec.BOOL.fieldOf("is_consumed").forGetter(InteractiveKeyComponent::is_consumed),
							Identifier.CODEC.listOf().fieldOf("identifier_list").forGetter(InteractiveKeyComponent::identifier_list)
					)
					.apply(instance, InteractiveKeyComponent::new)
	);
	public static final PacketCodec<RegistryByteBuf, InteractiveKeyComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL,
			InteractiveKeyComponent::is_consumed,
			Identifier.PACKET_CODEC.collect(PacketCodecs.toList()),
			InteractiveKeyComponent::identifier_list,
			InteractiveKeyComponent::new
	);

	public InteractiveKeyComponent(
			boolean is_consumed,
			List<Identifier> identifier_list
	) {
		this.is_consumed = is_consumed;
		this.identifier_list = identifier_list != null ? identifier_list : new ArrayList<>();
	}
}
